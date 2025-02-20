import { useEffect, useState } from "react";
import { getProducts, Product } from "../api/products";
import { useAuthStore } from "../store/authStore.ts";
import { useCartStore } from "../store/cartStore.ts";

export default function Products() {
    const [products, setProducts] = useState<Product[]>([]);
    const { token } = useAuthStore();
    const userEmail = useAuthStore((state) => state.tokenEmail);
    const { addItem } = useCartStore();

    useEffect(() => {
        async function fetchData() {
            const data = await getProducts();
            setProducts(data);
        }
        fetchData();
    }, []);

    //  Funktion zur Erstellung einer Blob-URL für das Bild
    const openImageInNewTab = (base64String: string) => {
        if (!base64String) return;
        const byteCharacters = atob(base64String.split(",")[1]);
        const byteNumbers = new Array(byteCharacters.length);
        for (let i = 0; i < byteCharacters.length; i++) {
            byteNumbers[i] = byteCharacters.charCodeAt(i);
        }
        const byteArray = new Uint8Array(byteNumbers);
        const blob = new Blob([byteArray], { type: "image/png" });
        const blobUrl = URL.createObjectURL(blob);
        window.open(blobUrl, "_blank");
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
                                    onClick={() => openImageInNewTab(productImages[0])}
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
                                        onClick={() => openImageInNewTab(image)}
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
        </div>
    );
}
