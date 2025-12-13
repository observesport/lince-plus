// Lince PLUS Desktop - Modern JavaScript

// API helper function
async function fetchAPI(url, method = 'GET', data = null) {
    try {
        const options = {
            method: method,
            headers: {
                'Content-Type': 'application/json',
            }
        };

        if (data && method !== 'GET') {
            options.body = JSON.stringify(data);
        }

        const response = await fetch(url, options);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('API Error:', error);
        throw error;
    }
}

// Open update dialog
function openUpdateLincePlus() {
    fetch('/component/update', {
        method: 'GET'
    })
    .then(response => {
        console.log('Opening updater');
    })
    .catch(error => {
        console.error('Update error:', error);
    });
}

// Update project statistics
async function updateStats() {
    try {
        const data = await fetchAPI('/session/getProjectInfo', 'GET');
        console.log('Getting project info:', data);

        // Update stats
        updateElement('statScene', data.scenes);
        updateElement('statVideo', data.videos);
        updateElement('projectUri', data.url);
        updateElement('statObserver', data.observers);

        // Show project info
        showElement('projectInfo');

        // Update i18n messages
        if (data.i18n) {
            const i18nIds = ['lang_qr', 'lang_desc_1', 'lang_desc_2', 'option_registers', 'option_observers'];
            i18nIds.forEach(id => {
                const message = data.i18n[id];
                if (message) {
                    updateElement(id, message);
                }
            });
        }

        // Update notification
        if (data.update && data.version) {
            updateElement('updateVersion', data.version);
            showElement('updateMessageWrapper');
        }

        // User information
        if (data.information && data.information.userInformation) {
            const messages = data.information.userInformation.map(item => {
                return `<div class="mb-2">
                    <strong>${item.title}:</strong> <span>${item.description}</span>
                </div>`;
            }).join('');

            if (messages) {
                updateElement('informationMessage', messages, true);
                showElement('informationMessageWrapper');
            }
        }

        // Refresh QR code
        refreshQRCode();

    } catch (error) {
        console.error('Error updating stats:', error);
    }
}

// Refresh QR code with cache busting
function refreshQRCode() {
    const timestamp = new Date().getTime();
    const qrImage = document.getElementById('qrCodeImage');
    if (qrImage) {
        qrImage.src = `/component/getQRCode?t=${timestamp}`;
    }
}

// Helper function to update element content
function updateElement(id, content, isHTML = false) {
    const element = document.getElementById(id);
    if (element) {
        if (isHTML) {
            element.innerHTML = content;
        } else {
            element.textContent = content;
        }
    }
}

// Helper function to show element
function showElement(id) {
    const element = document.getElementById(id);
    if (element) {
        element.classList.remove('hidden');
    }
}

// Helper function to hide element
function hideElement(id) {
    const element = document.getElementById(id);
    if (element) {
        element.classList.add('hidden');
    }
}

// Scroll to top functionality
function initScrollToTop() {
    const scrollButton = document.getElementById('scrollToTop');

    if (!scrollButton) return;

    // Show/hide button based on scroll position
    window.addEventListener('scroll', () => {
        if (window.pageYOffset > 300) {
            scrollButton.classList.remove('hidden');
        } else {
            scrollButton.classList.add('hidden');
        }
    });

    // Scroll to top on click
    scrollButton.addEventListener('click', () => {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });
}

// Initialize application
function initApp() {
    // Set current year
    const currentYear = new Date().getFullYear();
    updateElement('currentYear', currentYear);

    // Initial stats update
    updateStats();

    // Update stats every minute
    setInterval(updateStats, 60000);

    // Initialize scroll to top button
    initScrollToTop();

    // Remove preload class to enable transitions
    setTimeout(() => {
        document.body.classList.remove('preload');
    }, 100);

    console.log('Lince PLUS Desktop initialized');
}

// Wait for DOM to be ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initApp);
} else {
    initApp();
}

// Expose functions to global scope for inline event handlers
window.openUpdateLincePlus = openUpdateLincePlus;
window.refreshQRCode = refreshQRCode;