import { useEffect, useState } from "react";
import {getProducts, Product} from "../api/products";
import {useAuthStore} from "../store/authStore.ts";
import {useCartStore} from "../store/cartStore.ts";




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
        const byteCharacters = atob(base64String.split(",")[1]); // Entfernt das "data:image/png;base64," usw.
        const byteNumbers = new Array(byteCharacters.length);
        for (let i = 0; i < byteCharacters.length; i++) {
            byteNumbers[i] = byteCharacters.charCodeAt(i);
        }
        const byteArray = new Uint8Array(byteNumbers);
        const blob = new Blob([byteArray], { type: "image/png" }); // Erstelle eine Bild-Blob-Datei
        const blobUrl = URL.createObjectURL(blob);
        window.open(blobUrl, "_image"); // Öffne das Bild in einem neuen Tab
    };

    return (
        <div className="products-container">
            <h2 className="products-title">Produkte</h2>
            <div className="products-grid">
                {products.map((product) => (
                    <div key={product.id} className="product-card">
                        <img
                            src={product.imageBase64}
                            alt={product.name}
                            className="product-image"
                            onClick={() => openImageInNewTab(product.imageBase64)} // Öffne Bild korrekt
                        />
                        <h3 className="product-name">{product.name}</h3>
                        <p className="product-description-home">{product.description}</p>
                        <p className="product-price">{product.price} €</p>
                        <button
                            onClick={() => addItem(token!, userEmail!, {
                                productId: product.id,
                                name: product.name,
                                imageBase64: product.imageBase64,
                                quantity: 1,
                                price: product.price
                            })}
                            className="add-to-cart-button"
                        >
                            🛒 In den Warenkorb
                        </button>
                    </div>
                ))}
            </div>

        </div>
    );
}