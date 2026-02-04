// Simple Scroll Reveal Animation
document.addEventListener('DOMContentLoaded', () => {
    const observerOptions = {
        threshold: 0.1
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('fade-in');
            }
        });
    }, observerOptions);

    document.querySelectorAll('.feature-card, .quote-container').forEach(el => {
        observer.observe(el);
    });

    // Mobile Star Field Animation
    const container = document.querySelector('.stars-container');
    if (container) {
        for (let i = 0; i < 150; i++) {
            const star = document.createElement('div');
            star.className = 'star';
            const x = Math.random() * 100;
            const y = Math.random() * 100;
            const size = Math.random() * 2;
            const speed = Math.random() * 3 + 2;
            
            star.style.cssText = `
                position: absolute;
                left: ${x}%;
                top: ${y}%;
                width: ${size}px;
                height: ${size}px;
                background: white;
                border-radius: 50%;
                opacity: ${Math.random()};
                animation: twinkle ${speed}s infinite;
            `;
            container.appendChild(star);
        }
    }
});

// Twinkle Keyframes Injection
const style = document.createElement('style');
style.innerHTML = `
@keyframes twinkle {
    0%, 100% { opacity: 0.3; }
    50% { opacity: 1; }
}
`;
document.head.appendChild(style);
