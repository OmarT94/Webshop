import { useEffect, useState } from "react";
import { getProducts, addProduct, updateProduct, deleteProduct, Product } from "../api/products";
import { useAuthStore } from "../store/authStore";
import { useNavigate } from "react-router-dom";

export default function Manage() {
    const [products, setProducts] = useState<Product[]>([]);
    const [newProduct, setNewProduct] = useState<Omit<Product, "id">>({
        name: "",
        description: "",
        price: 0,
        stock: 0,
        image: "",
    });

    const token = useAuthStore((state) => state.token);
    const isAdmin = useAuthStore((state) => state.isAdmin);
    const navigate = useNavigate();

    useEffect(() => {
        if (!isAdmin) {
            navigate("/");
        }
    }, [isAdmin, navigate]);

    useEffect(() => {
        async function fetchData() {
            try {
                const data = await getProducts();

                // Füge Standardwerte für Produkte hinzu
                const validatedData = data.map((product: Product) => ({
                    ...product,
                    name: product.name || "",
                    description: product.description || "",
                    price: product.price || 0,
                    stock: product.stock || 0,
                    image: product.image || "",
                }));

                setProducts(validatedData);
            } catch (error) {
                console.error("Fehler beim Laden der Produkte:", error);
            }
        }
        fetchData();
        console.log(products);
    }, []);

    const handleAddProduct = async () => {
        if (!token) return;

        if (!newProduct.name || newProduct.price <= 0) {
            alert("Bitte gültige Produktdaten eingeben!");
            return;
        }

        try {
            const addedProduct = await addProduct(token, newProduct);
            setProducts([...products, addedProduct]);
            setNewProduct({ name: "", description: "", price: 0, stock: 0, image: "" });
        } catch (error) {
            console.error("Fehler beim Hinzufügen des Produkts:", error);
        }
    };

    const handleDelete = async (id: string) => {
        if (!token) return;
        try {
            await deleteProduct(token, id);
            setProducts((prevProducts) => prevProducts.filter((p) => p.id !== id));
        } catch (error) {
            console.error("Fehler beim Löschen:", error);
        }
    };

    const handleUpdate = async (id: string) => {
        if (!token) return;
        try {
            const productToUpdate = products.find((p) => p.id === id);
            if (!productToUpdate) {
                console.error("Produkt nicht gefunden!");
                return;
            }

            const updatedProduct = await updateProduct(token, id, productToUpdate);

            setProducts((prevProducts) =>
                prevProducts.map((p) => (p.id === id ? updatedProduct : p))
            );
        } catch (error) {
            console.error("Fehler beim Aktualisieren:", error);
        }
    };

    const handleChange = (id: string, field: keyof Product, value: string | number) => {
        console.log(`🔍 Änderung an Produkt mit ID ${id}: ${field} → ${value}`);

        setProducts((prevProducts) =>
            prevProducts.map((p) => {
                console.log(`➡️ Prüfe Produkt-ID: ${p.id} (Erwartet: ${id})`);
                return p.id === id ? { ...p, [field]: value } : p;
            })
        );
    };




    return (
        <div className="p-6 flex flex-col items-center">
            <h2 className="text-2xl font-bold">Admin-Produktverwaltung</h2>

            <div className="mt-6 border p-4 w-full max-w-lg">
                <h3 className="font-semibold">Neues Produkt hinzufügen</h3>
                <input type="text" placeholder="Name" value={newProduct.name} onChange={(e) => setNewProduct({ ...newProduct, name: e.target.value })} />
                <input type="text" placeholder="Beschreibung" value={newProduct.description} onChange={(e) => setNewProduct({ ...newProduct, description: e.target.value })} />
                <input type="number" placeholder="Preis" value={newProduct.price} onChange={(e) => setNewProduct({ ...newProduct, price: parseFloat(e.target.value) })} />
                <input type="number" placeholder="Stock" value={newProduct.stock} onChange={(e) => setNewProduct({ ...newProduct, stock: parseInt(e.target.value) })} />
                <input type="text" placeholder="Bild-URL" value={newProduct.image} onChange={(e) => setNewProduct({ ...newProduct, image: e.target.value })} />
                <button className="p-2 bg-green-500 text-white rounded mt-2" onClick={handleAddProduct}>
                    Produkt hinzufügen
                </button>
            </div>

            <ul className="mt-6 w-full max-w-lg">
                {products.map((product) => (
                    <li key={product.id} className="p-4 border rounded-lg shadow-lg mb-4 flex flex-col gap-2">
                        <input type="text" value={product.name} onChange={(e) => handleChange(product.id, "name", e.target.value)} />
                        <input type="text" value={product.description} onChange={(e) => handleChange(product.id, "description", e.target.value)} />
                        <input type="number" value={product.price} onChange={(e) => handleChange(product.id, "price", parseFloat(e.target.value))} />
                        <input type="number" value={product.stock} onChange={(e) => handleChange(product.id, "stock", parseInt(e.target.value))} />
                        <input type="text" value={product.image} onChange={(e) => handleChange(product.id, "image", e.target.value)} />

                        {product.image && <img src={product.image} alt={product.name} className="w-32 h-32 object-cover mt-2" />}

                        <button className="p-2 bg-blue-500 text-white rounded mr-2" onClick={() => handleUpdate(product.id)}>
                            Speichern
                        </button>
                        <button className="p-2 bg-red-500 text-white rounded" onClick={() => handleDelete(product.id)}>
                            Löschen
                        </button>
                    </li>
                ))}
            </ul>
        </div>
    );
}
