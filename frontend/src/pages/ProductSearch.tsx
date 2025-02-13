import { useState, useEffect } from "react";
import { getProductsByName, getProductsByDescription, getProductsByPrice } from "../api/products";

export default function ProductSearch() {
    const [name, setName] = useState("");
    const [category, setCategory] = useState("");
    const [minPrice, setMinPrice] = useState("0");
    const [maxPrice, setMaxPrice] = useState("");
    const [products, setProducts] = useState<any[]>([]);

    useEffect(() => {
        document.title = "Produktsuche";
    }, []);

    const handleNameSearch = async () => {
        if (!name.trim()) return;
        try {
            const data = await getProductsByName(name);
            setProducts(data);
        } catch (error) {
            console.error("Fehler beim Laden der Produkte nach Name:", error);
        }
    };

    const handleCategorySearch = async () => {
        if (!category.trim()) return;
        try {
            const data = await getProductsByDescription(category);
            setProducts(data);
        } catch (error) {
            console.error("Fehler beim Laden der Produkte nach Kategorie:", error);
        }
    };

    const handlePriceSearch = async () => {
        const min = Math.max(0, parseFloat(minPrice)); //  Keine negativen Preise
        const max = parseFloat(maxPrice) || 100000; //  Standard max falls leer
        try {
            const data = await getProductsByPrice(min, max);
            setProducts(data);
        } catch (error) {
            console.error("Fehler beim Laden der Produkte nach Preis:", error);
        }
    };

    return (
        <div className="p-6">
            <h2 className="text-2xl font-bold text-center">🔍 Produktsuche</h2>

            <div className="mt-4 flex flex-col gap-4 max-w-md mx-auto">
                <div>
                    <input
                        type="text"
                        placeholder="Produktsuche nach Name"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        className="border p-2 w-full"
                    />
                    <button onClick={handleNameSearch} className="bg-blue-500 text-white p-2 w-full mt-2">Suchen</button>
                </div>

                <div>
                    <input
                        type="text"
                        placeholder="Suche nach Kategorie"
                        value={category}
                        onChange={(e) => setCategory(e.target.value)}
                        className="border p-2 w-full"
                    />
                    <button onClick={handleCategorySearch} className="bg-green-500 text-white p-2 w-full mt-2">Suchen</button>
                </div>

                <div>
                    <input
                        type="number"
                        placeholder="Min Preis (≥ 0)"
                        value={minPrice}
                        onChange={(e) => setMinPrice(e.target.value)}
                        className="border p-2 w-full"
                        min="0"
                    />
                    <input
                        type="number"
                        placeholder="Max Preis"
                        value={maxPrice}
                        onChange={(e) => setMaxPrice(e.target.value)}
                        className="border p-2 w-full mt-2"
                    />
                    <button onClick={handlePriceSearch} className="bg-purple-500 text-white p-2 w-full mt-2">Suchen</button>
                </div>
            </div>

            <div className="mt-6 max-w-md mx-auto">
                <h3 className="text-xl font-bold text-center">🛒 Suchergebnisse</h3>
                <ul className="mt-4 border p-4 rounded-lg">
                    {products.length > 0 ? (
                        products.map((product: any) => (
                            <li key={product.id} className="border-b p-2 flex items-center gap-4">
                                <img src={product.imageBase64} alt={product.name} className="w-16 h-16 object-cover rounded" />
                                <div>
                                    <p className="font-bold">{product.name}</p>
                                    <p className="text-gray-500">{product.description}</p>
                                    <p className="text-green-500 font-semibold">{product.price}€</p>
                                </div>
                            </li>
                        ))
                    ) : (
                        <p className="text-center text-gray-500">Keine Produkte gefunden</p>
                    )}
                </ul>
            </div>
        </div>
    );
}
