// ===========================
// WastePlatform - Main JS
// ===========================

document.addEventListener('DOMContentLoaded', function() {

    // Auto-hide alerts sau 5 giây
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) bsAlert.close();
        }, 5000);
    });

    // Confirm trước khi thực hiện action quan trọng
    document.querySelectorAll('[data-confirm]').forEach(btn => {
        btn.addEventListener('click', function(e) {
            if (!confirm(this.dataset.confirm)) {
                e.preventDefault();
            }
        });
    });

    // Preview image khi upload
    const imageInputs = document.querySelectorAll('input[type="file"][accept="image/*"]');
    imageInputs.forEach(input => {
        input.addEventListener('change', function() {
            const preview = document.getElementById('imagePreview');
            if (preview && this.files[0]) {
                const reader = new FileReader();
                reader.onload = e => {
                    preview.src = e.target.result;
                    preview.classList.remove('d-none');
                };
                reader.readAsDataURL(this.files[0]);
            }
        });
    });

    // Active navbar link highlight
    const currentPath = window.location.pathname;
    document.querySelectorAll('.nav-link').forEach(link => {
        if (link.getAttribute('href') === currentPath) {
            link.classList.add('active', 'fw-bold');
        }
    });
});

// Helper: Show toast notification
function showToast(message, type = 'success') {
    const toastHtml = `
        <div class="toast align-items-center text-white bg-${type} border-0 position-fixed bottom-0 end-0 m-3"
             role="alert" style="z-index:9999">
            <div class="d-flex">
                <div class="toast-body">${message}</div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" 
                        data-bs-dismiss="toast"></button>
            </div>
        </div>`;
    document.body.insertAdjacentHTML('beforeend', toastHtml);
    const toast = document.querySelector('.toast:last-child');
    bootstrap.Toast.getOrCreateInstance(toast).show();
}