import { useEffect, useState } from "react";
import { getProducts, Product } from "../api/products";
import { useAuthStore } from "../store/authStore.ts";
import { useCartStore } from "../store/cartStore.ts";

export default function Products() {
    const [products, setProducts] = useState<Product[]>([]);
    const { token } = useAuthStore();
    const userEmail = useAuthStore((state) => state.tokenEmail);
    const { addItem } = useCartStore();
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [currentImageIndex, setCurrentImageIndex] = useState(0);
    const [currentProductImages, setCurrentProductImages] = useState<string[]>([]);

    useEffect(() => {
        async function fetchData() {
            const data = await getProducts();
            setProducts(data);
        }
        fetchData();
    }, []);

    const openImageInModal = (images: string[], index: number) => {
        setCurrentProductImages(images);
        setCurrentImageIndex(index);
        setIsModalOpen(true);
    };

    const closeModal = () => {
        setIsModalOpen(false);
    };

    const goToNextImage = () => {
        setCurrentImageIndex((prevIndex) => (prevIndex + 1) % currentProductImages.length);
    };

    const goToPreviousImage = () => {
        setCurrentImageIndex((prevIndex) => (prevIndex - 1 + currentProductImages.length) % currentProductImages.length);
    };

    return (
        <div className="products-container">
            <h2 className="products-title">Produkte</h2>
            <div className="products-grid">
                {products.map((product) => {
                    const productImages = product.images && product.images.length > 0 ? product.images : [];
                    return (
                        <div key={product.id} className="product-card">
                            {/*  Hauptbild (Erstes Bild aus `images`) */}
                            {productImages.length > 0 ? (
                                <img
                                    src={productImages[0]}
                                    alt={product.name}
                                    className="product-image"
                                    onClick={() => openImageInModal(productImages, 0)}
                                />
                            ) : (
                                <p className="no-image-text">Kein Bild verfügbar</p>
                            )}

                            {/*  Bild-Galerie mit allen Bildern */}
                            <div className="product-gallery">
                                {productImages.length > 1 && productImages.map((image, index) => (
                                    <img
                                        key={index}
                                        src={image}
                                        alt={`${product.name} Bild ${index + 1}`}
                                        className="product-thumbnail"
                                        onClick={() => openImageInModal(productImages, index)}
                                    />
                                ))}
                            </div>

                            <h3 className="product-name">{product.name}</h3>
                            <p className="product-description-home">{product.description}</p>
                            <p className="product-price">{product.price} €</p>
                            <button
                                onClick={() => addItem(token!, userEmail!, {
                                    productId: product.id,
                                    name: product.name,
                                    images: productImages,
                                    quantity: 1,
                                    price: product.price
                                })}
                                className="add-to-cart-button"
                            >
                                🛒 In den Warenkorb
                            </button>
                        </div>
                    );
                })}
            </div>

            {/* Modal für die Bildergalerie */}
            {isModalOpen && (
                <div className="modal-overlay" onClick={closeModal}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <button className="close-modal-button" onClick={closeModal}>×</button>
                        <div className="modal-image-container">
                            <img
                                src={currentProductImages[currentImageIndex]}
                                alt={`Bild ${currentImageIndex + 1}`}
                                className="modal-image"
                            />
                        </div>
                        <div className="modal-navigation">
                            <button onClick={goToPreviousImage}>←</button>
                            <button onClick={goToNextImage}>→</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}