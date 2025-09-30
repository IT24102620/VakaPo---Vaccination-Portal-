package lk.vakapo.vakapo.Common;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class FileStorageService {
    private final Path root = Paths.get("uploads");

    // ---------- SAVE ----------
    public String saveUnderRoleAndUser(MultipartFile file, String role, String username) throws IOException {
        if (file == null || file.isEmpty()) return null;

        // Ensure root exists
        ensureDir(root);

        // Make subfolder: uploads/<ROLE>/<username>
        String safeRole = safePart(role);
        String safeUser = safePart(username);
        Path userDir = root.resolve(safeRole).resolve(safeUser);
        ensureDir(userDir);

        // Build unique filename with original extension
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.')); // includes the dot
        }
        String uniqueName = UUID.randomUUID().toString().replace("-", "") + ext;

        Path target = userDir.resolve(uniqueName);

        // Save (overwrite if somehow exists)
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // Return path relative to 'uploads/' (normalized with forward slashes)
        return root.relativize(target).toString().replace('\\', '/');
    }

    // ---------- DELETE ----------
    public void deleteUserFolder(String role, String username) throws IOException {
        String safeRole = safePart(role);
        String safeUser = safePart(username);
        Path userDir = root.resolve(safeRole).resolve(safeUser);

        if (Files.exists(userDir)) {
            // Delete recursively: walk the tree, sort in reverse (delete children first)
            try (Stream<Path> walk = Files.walk(userDir)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException ignored) {}
                        });
            }
        }
    }

    // ---------- UTIL ----------
    private void ensureDir(Path p) throws IOException {
        if (!Files.exists(p)) Files.createDirectories(p);
    }

    /** Keep alnum, dash, underscore; replace others with underscore to keep paths safe. */
    private String safePart(String s) {
        if (s == null || s.isBlank()) return "unknown";
        return s.replaceAll("[^a-zA-Z0-9-_]", "_");
    }
}
