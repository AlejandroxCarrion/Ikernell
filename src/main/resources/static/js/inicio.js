document.addEventListener('DOMContentLoaded', function() {
    const mobileMenuBtn = document.querySelector('.mobile-menu-btn');
    const menu = document.querySelector('.menu');
    
    // Crear un menú móvil que se mostrará como overlay
    const mobileMenu = document.createElement('div');
    mobileMenu.className = 'mobile-menu';
    
    // Clonar los elementos del menú principal
    const menuClone = menu.cloneNode(true);
    mobileMenu.appendChild(menuClone);
    
    // Añadir los botones de autenticación al menú móvil
    const authButtons = document.querySelector('.auth-buttons').cloneNode(true);
    mobileMenu.appendChild(authButtons);
    
    // Añadir el menú móvil al body
    document.body.appendChild(mobileMenu);
    
    // Botón de cierre para el menú móvil
    const closeBtn = document.createElement('button');
    closeBtn.className = 'close-menu-btn';
    closeBtn.innerHTML = '<i class="fas fa-times"></i>';
    mobileMenu.prepend(closeBtn);
    
    // Función para alternar la visibilidad del menú móvil
    mobileMenuBtn.addEventListener('click', function() {
        mobileMenu.classList.toggle('active');
        document.body.classList.toggle('menu-open'); // Para bloquear el scroll del body
    });
    
    // Cerrar el menú móvil al hacer clic en el botón de cierre
    closeBtn.addEventListener('click', function() {
        mobileMenu.classList.remove('active');
        document.body.classList.remove('menu-open');
    });
    
    // Cerrar el menú móvil al hacer clic en un enlace
    mobileMenu.querySelectorAll('a').forEach(function(link) {
        link.addEventListener('click', function() {
            mobileMenu.classList.remove('active');
            document.body.classList.remove('menu-open');
        });
    });
    
    // Services Carousel
    const servicesCarousel = document.querySelector('.services-carousel');
    const serviceItems = document.querySelectorAll('.service-item');
    const carouselDots = document.querySelector('.carousel-dots');
    const prevBtn = document.querySelector('.services .prev-btn');
    const nextBtn = document.querySelector('.services .next-btn');
    
    if (servicesCarousel && serviceItems.length > 0) {
        // Create dots based on number of service items
        for (let i = 0; i < serviceItems.length; i++) {
            const dot = document.createElement('div');
            dot.classList.add('dot');
            if (i === 0) dot.classList.add('active');
            
            dot.addEventListener('click', function() {
                showServiceSlide(i);
            });
            
            carouselDots.appendChild(dot);
        }
        
        let currentServiceSlide = 0;
        
        function showServiceSlide(index) {
            // Update active dot
            const dots = document.querySelectorAll('.services .carousel-dots .dot');
            dots.forEach(dot => dot.classList.remove('active'));
            dots[index].classList.add('active');
            
            // For mobile view, show only current slide
            if (window.innerWidth < 768) {
                serviceItems.forEach((item, i) => {
                    if (i === index) {
                        item.style.display = 'block';
                    } else {
                        item.style.display = 'none';
                    }
                });
            }
            
            currentServiceSlide = index;
        }
        
        // Initially show correct view based on screen size
        if (window.innerWidth < 768) {
            showServiceSlide(0);
        }
        
        // Handle next/prev buttons for mobile view
        if (prevBtn && nextBtn) {
            prevBtn.addEventListener('click', function() {
                let newIndex = currentServiceSlide - 1;
                if (newIndex < 0) newIndex = serviceItems.length - 1;
                showServiceSlide(newIndex);
            });
            
            nextBtn.addEventListener('click', function() {
                let newIndex = currentServiceSlide + 1;
                if (newIndex >= serviceItems.length) newIndex = 0;
                showServiceSlide(newIndex);
            });
        }
        
        // Update on window resize
        window.addEventListener('resize', function() {
            if (window.innerWidth < 768) {
                showServiceSlide(currentServiceSlide);
            } else {
                // Reset visibility for desktop view
                serviceItems.forEach(item => {
                    item.style.display = 'block';
                });
            }
        });
    }
    
    // Testimonials Carousel
    // Seleccionar elementos del DOM
    const testimonialsCarousel = document.querySelector('.testimonials-carousel');
    const testimonialItems = document.querySelectorAll('.testimonial-item');
    const dotsContainer = document.querySelector('.testimonials .carousel-dots');
    const testimonialprevBtn = document.querySelector('.testimonials .prev-btn');
    const testimonialnextBtn = document.querySelector('.testimonials .next-btn');
    
    // Variables de control
    let currentIndex = 0;
    const totalItems = testimonialItems.length;
    
    // Inicializar el carrusel
    function initCarousel() {
        // Crear puntos indicadores
        for (let i = 0; i < totalItems; i++) {
            const dot = document.createElement('span');
            dot.classList.add('dot');
            if (i === 0) dot.classList.add('active');
            dot.addEventListener('click', () => goToSlide(i));
            dotsContainer.appendChild(dot);
        }
        
        // Configurar el carrusel para mostrar solo el primer elemento
        updateCarousel();
        
        // Configurar los botones
        testimonialprevBtn.addEventListener('click', prevSlide);
        testimonialnextBtn.addEventListener('click', nextSlide);
        
        // Auto-reproducción
        let interval = setInterval(nextSlide, 5000);
        
        // Detener auto-reproducción al interactuar
        testimonialsCarousel.addEventListener('mouseenter', () => {
            clearInterval(interval);
        });
        
        testimonialsCarousel.addEventListener('mouseleave', () => {
            interval = setInterval(nextSlide, 5000);
        });
    }
    
    // Actualizar el carrusel
    function updateCarousel() {
        // Ocultar todos los elementos
        testimonialItems.forEach(item => {
            item.style.display = 'none';
            item.classList.remove('active');
        });
        
        // Mostrar el elemento actual
        testimonialItems[currentIndex].style.display = 'block';
        testimonialItems[currentIndex].classList.add('active');
        
        // Actualizar puntos indicadores
        const dots = dotsContainer.querySelectorAll('.dot');
        dots.forEach((dot, index) => {
            dot.classList.toggle('active', index === currentIndex);
        });
    }
    
    // Ir a la diapositiva anterior
    function prevSlide() {
        currentIndex = (currentIndex - 1 + totalItems) % totalItems;
        updateCarousel();
    }
    
    // Ir a la siguiente diapositiva
    function nextSlide() {
        currentIndex = (currentIndex + 1) % totalItems;
        updateCarousel();
    }
    
    // Ir a una diapositiva específica
    function goToSlide(index) {
        currentIndex = index;
        updateCarousel();
    }
    
    // Iniciar el carrusel si existen elementos
    if (testimonialItems.length > 0) {
        initCarousel();
    }
    
    // Añadir efecto de desvanecimiento para una transición suave
    function fadeIn(element) {
        element.style.opacity = 0;
        element.style.display = 'block';
        
        let opacity = 0;
        const timer = setInterval(() => {
            if (opacity >= 1) {
                clearInterval(timer);
            }
            element.style.opacity = opacity;
            opacity += 0.1;
        }, 30);
    }
    
    // Hacer que el carrusel sea responsive
    function handleResize() {
        const viewportWidth = window.innerWidth;
        
        if (viewportWidth < 768) {
            // En dispositivos móviles, mostrar un elemento a la vez
            testimonialsCarousel.style.gridTemplateColumns = '1fr';
        } else {
            // En pantallas más grandes, mostrar según el diseño original
            updateCarousel();
        }
    }
    
    // Escuchar cambios en el tamaño de la ventana
    window.addEventListener('resize', handleResize);
    
    // Inicializar el responsive
    handleResize();
    
    // FAQ Section Toggle
    const faqItems = document.querySelectorAll('.faq-item');

        if (faqItems.length > 0) {
            faqItems.forEach(item => {
                const question = item.querySelector('.faq-question');

                question.addEventListener('click', () => {
                    // Cerrar todos los otros elementos
                    faqItems.forEach(otherItem => {
                        if (otherItem !== item) {
                            otherItem.classList.remove('active');
                        }
                    });

                    // Alternar el estado activo del elemento actual
                    item.classList.toggle('active');
                });
            });
        }


    
    // Smooth scroll for anchor links
    const anchorLinks = document.querySelectorAll('a[href^="#"]');
    
    anchorLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            const targetId = this.getAttribute('href');
            
            // Skip empty links or just "#"
            if (targetId === '#' || targetId === '') return;
            
            const targetElement = document.querySelector(targetId);
            
            if (targetElement) {
                e.preventDefault();
                
                const yOffset = -80; // Header height offset
                const y = targetElement.getBoundingClientRect().top + window.pageYOffset + yOffset;
                
                window.scrollTo({top: y, behavior: 'smooth'});
                
                // Close mobile menu if open
                if (menu.classList.contains('active')) {
                    menu.classList.remove('active');
                    const icon = mobileMenuBtn.querySelector('i');
                    icon.classList.remove('fa-times');
                    icon.classList.add('fa-bars');
                }
            }
        });
    });
    
    // Newsletter form submission
    const newsletterForm = document.querySelector('.newsletter-form');
    
    if (newsletterForm) {
        newsletterForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const emailInput = this.querySelector('input[type="email"]');
            const email = emailInput.value.trim();
            
            if (email) {
                // Here you would normally send this to your backend
                // For demo purposes, we'll just show an alert
                alert('¡Gracias por suscribirte! Recibirás nuestras actualizaciones en ' + email);
                emailInput.value = '';
            }
        });
    }
    
    // Add active class to current menu item based on URL
    const currentLocation = location.pathname;
    const menuLinks = document.querySelectorAll('.menu a');
    const footerLinks = document.querySelectorAll('.footer-section.links a');
    
    function setActiveLink(links) {
        links.forEach(link => {
            const linkPath = link.getAttribute('href');
            
            if (currentLocation.indexOf(linkPath) !== -1 && linkPath !== 'index.html') {
                link.parentElement.classList.add('active');
            } else if (currentLocation.endsWith('/') && linkPath === 'index.html') {
                link.parentElement.classList.add('active');
            }
        });
    }
    
    setActiveLink(menuLinks);
    setActiveLink(footerLinks);
    
    // Add animation to elements when they become visible
    const animatedElements = document.querySelectorAll('.service-item, .about-content, .about-image, .testimonial-item, .browser-icon');
    
    function checkVisibility() {
        animatedElements.forEach(element => {
            const elementPosition = element.getBoundingClientRect();
            const windowHeight = window.innerHeight;
            
            if (elementPosition.top < windowHeight * 0.85) {
                element.classList.add('fadeInUp');
            }
        });
    }
    
    // Add fadeInUp class from CSS
    document.head.insertAdjacentHTML('beforeend', `
        <style>
            .fadeInUp {
                animation: fadeInUp 0.8s ease forwards;
            }
        </style>
    `);
    
    // Check visibility on load and scroll
    window.addEventListener('load', checkVisibility);
    window.addEventListener('scroll', checkVisibility);
    
    // Fix navigation on scroll
    const header = document.querySelector('header');
    const headerHeight = header.offsetHeight;
    
    window.addEventListener('scroll', function() {
        if (window.scrollY > headerHeight) {
            header.classList.add('fixed');
        } else {
            header.classList.remove('fixed');
        }
    });
    
    // Image loading optimization
    const images = document.querySelectorAll('img');
    
    images.forEach(img => {
        if (img.complete) {
            img.classList.add('loaded');
        } else {
            img.addEventListener('load', function() {
                img.classList.add('loaded');
            });
        }
    });
    
    // Fix logo path
    const logo = document.getElementById('logo');
    if (logo && logo.src.includes('../Assets/img/logo.png')) {
        // This ensures the logo works correctly whether viewed from the root or a subdirectory
        const currentPath = window.location.pathname;
        const isRoot = currentPath.endsWith('/') || currentPath.endsWith('index.html');
        
        if (isRoot) {
            logo.src = 'Assets/img/logo.png';
        }
    }
});