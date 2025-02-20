import { useState, useEffect } from "react";
import {
    getProductsByName,
    getProductsByPrice,
    getProducts,
    getProductsByCategory, Product
} from "../api/products";
import {useAuthStore} from "../store/authStore.ts";
import {useCartStore} from "../store/cartStore.ts";


export default function ProductSearch() {
    const { token } = useAuthStore();
    const userEmail = useAuthStore((state) => state.tokenEmail);
    const { addItem } = useCartStore();
    const [name, setName] = useState("");
    const [category, setCategory] = useState("");
    const [minPrice, setMinPrice] = useState("0");
    const [maxPrice, setMaxPrice] = useState("");
    const [products, setProducts] = useState<Product[]>([]);

    useEffect(() => {
        document.title = "Produktsuche";
    }, []);

    useEffect(() => {
        async function fetchData() {
            const data = await getProducts();
            setProducts(data);
        }
        fetchData();
    }, []);

    const handleNameSearch = async () => {
        if (!name.trim()) return;
        try {
            const data = await getProductsByName(name);
            console.log("API-Antwort für Name-Suche:", data); // Debugging
            setProducts([...new Map((data as Product[]).map(item => [item.id, item])).values()]);

        } catch (error) {
            console.error("Fehler beim Laden der Produkte nach Name:", error);
        }
    };

    const handleCategorySearch = async () => {
        if (!category.trim()) return;
        try {
            console.log("Gesendete Kategorie:", category); // Debugging
            const data = await getProductsByCategory(category);
            console.log("API-Antwort für Kategorie-Suche:", data); // Debugging
            setProducts([...new Map((data as Product[]).map(item => [item.id, item])).values()]);

        } catch (error) {
            console.error("Fehler beim Laden der Produkte nach Kategorie:", error);
        }
    };

    const handlePriceSearch = async () => {
        const min = Math.max(0, parseFloat(minPrice)); // Keine negativen Preise
        const max = parseFloat(maxPrice) || 100000; // Standard max, falls leer
        try {
            const data = await getProductsByPrice(min, max);
            console.log("API-Antwort für Preis-Suche:", data); // Debugging
            setProducts([...new Map((data as Product[]).map(item => [item.id, item])).values()]);
        } catch (error) {
            console.error("Fehler beim Laden der Produkte nach Preis:", error);
        }
    };

    // Debouncing für die Suche
    useEffect(() => {
        const delayDebounceFn = setTimeout(() => {
            const searchProducts = async () => {
                if (name.trim()) {
                    try {
                        const data = await getProductsByName(name);
                        console.log("Produkte nach Name:", data); // Debugging
                        setProducts(data);
                    } catch (error) {
                        console.error("Fehler beim Laden der Produkte nach Name:", error);
                    }
                } else if (category.trim()) {
                    try {
                        const data = await getProductsByCategory(category);
                        console.log("Produkte nach Kategorie:", data); // Debugging
                        setProducts(data);
                    } catch (error) {
                        console.error("Fehler beim Laden der Produkte nach Kategorie:", error);
                    }
                } else if (minPrice.trim() || maxPrice.trim()) {
                    const min = Math.max(0, parseFloat(minPrice)); // Keine negativen Preise
                    const max = parseFloat(maxPrice) || 100000; // Standard max, falls leer
                    try {
                        const data = await getProductsByPrice(min, max);
                        console.log("Produkte nach Preis:", data); // Debugging
                        setProducts(data);
                    } catch (error) {
                        console.error("Fehler beim Laden der Produkte nach Preis:", error);
                    }
                } else {
                    // Wenn alle Filter leer sind, lade alle Produkte

                }
            };

            searchProducts();
        }, 500); // 500ms Verzögerung

        return () => clearTimeout(delayDebounceFn);
    }, [name, category, minPrice, maxPrice]);

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
        <div className="Search-HomePage-Container">
            <h2 className="Search-HomePage-Heading">🔍 Produktsuche</h2>

            <div className="Search-HomePage-SearchContainer">
                <div className="Search-HomePage-SearchGroup">
                    <input
                        type="text"
                        placeholder="Produktsuche nach Name"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        className="Search-HomePage-Input"
                    />
                    <button onClick={handleNameSearch} className="Search-HomePage-Button Search-HomePage-ButtonBlue">Suchen</button>
                </div>

                <div className="Search-HomePage-SearchGroup">
                    <input
                        type="text"
                        placeholder="Suche nach Kategorie"
                        value={category}
                        onChange={(e) => setCategory(e.target.value)}
                        className="Search-HomePage-Input"
                    />
                    <button onClick={handleCategorySearch} className="Search-HomePage-Button Search-HomePage-ButtonGreen">Suchen</button>
                </div>

                <div className="Search-HomePage-SearchGroup">
                    <input
                        type="number"
                        placeholder="Min Preis (≥ 0)"
                        value={minPrice}
                        onChange={(e) => setMinPrice(e.target.value)}
                        className="Search-HomePage-Input"
                        min="0"
                    />
                    <input
                        type="number"
                        placeholder="Max Preis"
                        value={maxPrice}
                        onChange={(e) => setMaxPrice(e.target.value)}
                        className="Search-HomePage-Input Search-HomePage-InputMargin"
                    />
                    <button onClick={handlePriceSearch} className="Search-HomePage-Button Search-HomePage-ButtonPurple">Suchen</button>
                </div>
            </div>

            <div className="products-container">
                <h3 className="products-title">🛒 Suchergebnisse</h3>
                <ul className={`products-grid ${products.length === 1 ? "single-product" : ""}`}>
                    {products.length > 0 ? (
                        products.map((product: any) => (
                            <li key={product.id} className="product-card">
                                <img
                                    src={product.images[0] || "/path/to/placeholder-image.png"} // Fallback für fehlende Bilder
                                    alt={product.name}
                                    className="product-image"
                                    onClick={() => openImageInNewTab(product.images[0])}
                                />
                                <div>
                                    <p className="product-name">{product.name}</p>
                                    <p className="product-description">{product.description}</p>
                                    <p className="product-price">{product.price}€</p>

                                    {/* 🛒 Button zum Warenkorb hinzufügen */}
                                    <button
                                        onClick={() => {
                                            console.log("🛒 Produkt zum Warenkorb hinzufügen:", {
                                                productId: product.id,
                                                name: product.name,
                                                images: product.images,
                                                quantity: 1,
                                                price: product.price
                                            });

                                            addItem(token!, userEmail!, {
                                                productId: product.id,
                                                name: product.name,
                                                images: product.images,
                                                quantity: 1,
                                                price: product.price
                                            });
                                        }}
                                        className="add-to-cart-button"
                                    >
                                        🛒 In den Warenkorb
                                    </button>

                                </div>
                            </li>
                        ))
                    ) : (
                        <p className="Search-HomePage-NoResults">Keine Produkte gefunden</p>
                    )}
                </ul>

            </div>
        </div>
    );
}