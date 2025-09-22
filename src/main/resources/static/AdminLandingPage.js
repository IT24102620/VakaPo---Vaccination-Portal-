(function () {
    const sidebar = document.getElementById('sidebar');
    const menuToggle = document.getElementById('menuToggle');
    const sidebarClose = document.getElementById('sidebarClose');
    const overlay = document.getElementById('overlay');
    const body = document.body;

    // Show/hide mobile menu toggle button
    function updateToggleVisibility() {
        const isMobile = window.matchMedia('(max-width:768px)').matches;
        menuToggle.style.display = isMobile ? 'block' : 'none';
    }
    updateToggleVisibility();
    window.addEventListener('resize', updateToggleVisibility);

    // Desktop hover expand/collapse
    function onEnter() {
        if (window.innerWidth > 768) body.classList.add('sidebar-expanded');
    }
    function onLeave() {
        if (window.innerWidth > 768) body.classList.remove('sidebar-expanded');
    }
    sidebar.addEventListener('mouseenter', onEnter);
    sidebar.addEventListener('mouseleave', onLeave);

    // Mobile open
    menuToggle.addEventListener('click', () => {
        sidebar.classList.add('open');
        overlay.classList.add('active');
        body.style.overflow = 'hidden'; // disable scroll
    });

    // Close function (mobile)
    function closeMobileSidebar() {
        sidebar.classList.remove('open');
        overlay.classList.remove('active');
        body.style.overflow = '';
    }
    sidebarClose.addEventListener('click', closeMobileSidebar);
    overlay.addEventListener('click', closeMobileSidebar);

    // Close on Escape
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            if (sidebar.classList.contains('open')) closeMobileSidebar();
            body.classList.remove('sidebar-expanded');
        }
    });

    // Handle resize transitions
    window.addEventListener('resize', () => {
        if (window.innerWidth > 768) {
            overlay.classList.remove('active');
            sidebar.classList.remove('open');
            body.style.overflow = '';
        } else {
            body.classList.remove('sidebar-expanded');
        }
    });

    // Auto-close mobile sidebar when clicking a link
    document.querySelectorAll('.sidebar-link').forEach(link => {
        link.addEventListener('click', () => {
            if (window.innerWidth <= 768) closeMobileSidebar();
        });
    });
})();
