package lk.vakapo.vakapo.Common;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path root = Paths.get("uploads");

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

    /** Optional helper if you want to save into custom subfolders: uploads/<subfolders...>/<UUID>.<ext> */
    public String saveUnder(String... subfoldersAndFilenameFromMultipart) {
        throw new UnsupportedOperationException("Not implemented in this minimal version");
    }

    private void ensureDir(Path p) throws IOException {
        if (!Files.exists(p)) Files.createDirectories(p);
    }

    /** Keep alnum, dash, underscore; replace others with underscore to keep paths safe. */
    private String safePart(String s) {
        if (s == null || s.isBlank()) return "unknown";
        return s.replaceAll("[^a-zA-Z0-9-_]", "_");
    }
}
