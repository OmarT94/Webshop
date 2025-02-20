import { useEffect, useState } from "react";
import { getProducts, addProduct, updateProduct, deleteProduct, Product } from "../api/products";
import { useAuthStore } from "../store/authStore";
import { useNavigate } from "react-router-dom";
import {addCategory, getCategories} from "../api/categories.ts";



export default function Manage() {
    const [products, setProducts] = useState<Product[]>([]);
    const [filteredProducts, setFilteredProducts] = useState<Product[]>([]);
    const [categories, setCategories] = useState<{ id: string; name: string }[]>([]);
    const [newCategory, setNewCategory] = useState(""); //  Neue Kategorie

    //  Suchfilter für Name, Preisbereich, Kategorie
    const [searchTerm, setSearchTerm] = useState("");
    const [minPrice, setMinPrice] = useState<number | "">("");
    const [maxPrice, setMaxPrice] = useState<number | "">("");
    const [category, setCategory] = useState("");


    const [newProduct, setNewProduct] = useState<Omit<Product, "id">>({
        name: "",
        description: "",
        price: 0,
        stock: 0,
        images: [],
        categoryId: "",
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
        async function fetchCategories() {
            try {
                const data = await getCategories();
                console.log("Geladene Kategorien:", data);  //  Debugging
                setCategories(data);
            } catch (error) {
                console.error("Fehler beim Laden der Kategorien:", error);
            }
        }
        fetchCategories();
    }, []);

    useEffect(() => {
        async function fetchData() {
            try {
                const data = await getProducts();
                setProducts(data);
                setFilteredProducts(data);
            } catch (error) {
                console.error("Fehler beim Laden der Produkte:", error);
            }
        }
        fetchData();
    }, []);

    //  Produktsuche mit mehreren Filtern (Name, Preisbereich, Kategorie)
    useEffect(() => {
        let filtered = products;

        if (searchTerm.trim()) {
            filtered = filtered.filter((p) =>
                p.name.toLowerCase().includes(searchTerm.toLowerCase())
            );
        }

        if (minPrice !== "") {
            filtered = filtered.filter((p) => p.price >= Number(minPrice));
        }

        if (maxPrice !== "") {
            filtered = filtered.filter((p) => p.price <= Number(maxPrice));
        }

        if (category.trim()) {
            filtered = filtered.filter((p) => p.categoryId.toLowerCase().includes(category.toLowerCase()));
        }

        setFilteredProducts(filtered);
    }, [searchTerm, minPrice, maxPrice, category, products]);

    //  Neue Kategorie erstellen
    const handleAddCategory = async () => {
        if (!newCategory.trim()) return;

        //  Korrekte Prüfung: `categories.some()` statt `includes()`
        if (categories.some(cat => cat.name === newCategory)) {
            alert("Kategorie existiert bereits!");
            return;
        }

        try {
            const addedCategory = await addCategory(newCategory);

            // setCategories muss { id, name } speichern
            setCategories([...categories, { id: addedCategory.id, name: addedCategory.name }]);

            setNewCategory("");
        } catch (error) {
            console.error("Fehler beim Hinzufügen der Kategorie:", error);
        }
    };


    const handleAddProduct = async () => {
        if (!token) return;

        if (!newProduct.name || newProduct.price <= 0 || newProduct.images.length === 0 || !newProduct.categoryId) {
            alert("Bitte gültige Produktdaten eingeben!");
            return;
        }

        try {
            //  Jetzt sucht TypeScript in einem Array von Objekten mit `id` und `name`
            const category = categories.find(cat => cat.id === newProduct.categoryId);
            if (!category) {
                alert("Kategorie existiert nicht!");
                return;
            }

            // Produkt mit richtiger categoryId senden
            const addedProduct = await addProduct(token, {
                ...newProduct,
                categoryId: category.id
            });

            setProducts([...products, addedProduct]);
            setNewProduct({ name: "", description: "", price: 0, stock: 0, images: [], categoryId: "" });
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
        setProducts((prevProducts) =>
            prevProducts.map((p) => (p.id === id ? { ...p, [field]: value } : p))
        );
    };

    //  Datei als Base64 konvertieren
    const handleFiles = (e: React.ChangeEvent<HTMLInputElement>) => {
        const files = e.target.files;
        if (!files) return;

        const promises = Array.from(files).map(file => {
            return new Promise<string>((resolve) => {
                const reader = new FileReader();
                reader.readAsDataURL(file);
                reader.onload = () => resolve(reader.result as string);
            });
        });

        Promise.all(promises).then(base64Images => {
            setNewProduct({ ...newProduct, images: base64Images });
        });
    };

    //  NEU: Bild für existierende Produkte ändern
    const handleMultipleImageChange = (id: string, files: FileList) => {
        const promises = Array.from(files).map(file => {
            return new Promise<string>((resolve) => {
                const reader = new FileReader();
                reader.readAsDataURL(file);
                reader.onload = () => resolve(reader.result as string);
            });
        });

        Promise.all(promises).then(base64Images => {
            setProducts((prevProducts) =>
                prevProducts.map((p) =>
                    p.id === id
                        ? { ...p, images: [...(p.images || []), ...base64Images] } //  Mehrere Bilder gleichzeitig hinzufügen
                        : p
                )
            );
        });
    };

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
        <div className="admin-container">
            <h2 className="admin-title">Admin-Produktverwaltung</h2>

            {/*  Erweiterte Suchfelder */}
            <div className="search-container">
                <input type="text" placeholder="🔍 Produktname" value={searchTerm}
                       onChange={(e) => setSearchTerm(e.target.value)}/>
                <input type="number" placeholder="Min Preis" value={minPrice}
                       onChange={(e) => setMinPrice(e.target.value ? Number(e.target.value) : "")}/>
                <input type="number" placeholder="Max Preis" value={maxPrice}
                       onChange={(e) => setMaxPrice(e.target.value ? Number(e.target.value) : "")}/>
                <select value={category} onChange={(e) => setCategory(e.target.value)}>
                    <option value="">Alle Kategorien</option>
                    {categories.length > 0 ? (
                        categories.map((cat) => (
                            <option key={cat.id} value={cat.id}>{cat.name}</option>  //  Name anzeigen, ID speichern
                        ))
                    ) : (
                        <option disabled>Lädt Kategorien...</option>
                    )}
                </select>


            </div>

            {/*  Neue Kategorie hinzufügen */}
            <div className="category-form">
                <h3>Kategorie hinzufügen</h3>
                <input type="text" placeholder="Neue Kategorie" value={newCategory}
                       onChange={(e) => setNewCategory(e.target.value)}/>
                <button className="add-category-button" onClick={handleAddCategory}>Kategorie hinzufügen</button>
            </div>

            <div className="product-form">
                <h3>Neues Produkt hinzufügen</h3>
                <input type="text" placeholder="Name" value={newProduct.name}
                       onChange={(e) => setNewProduct({...newProduct, name: e.target.value})}/>
                <textarea placeholder="Beschreibung"
                          value={newProduct.description}
                          onChange={(e) => setNewProduct({...newProduct, description: e.target.value})}
                          className="product-description"
                />
                <input type="number" placeholder="Preis" value={newProduct.price}
                       onChange={(e) => setNewProduct({...newProduct, price: parseFloat(e.target.value)})}/>
                <input type="number" placeholder="Stock" value={newProduct.stock}
                       onChange={(e) => setNewProduct({...newProduct, stock: parseInt(e.target.value)})}/>
                <input type="file" accept="image/*" multiple onChange={handleFiles}/>

                {/* KATEGORIE AUSWÄHLEN */}
                <select value={newProduct.categoryId}
                        onChange={(e) => setNewProduct({...newProduct, categoryId: e.target.value})}>
                    <option value="">Kategorie auswählen</option>
                    {categories.map((cat) => (
                        <option key={cat.id} value={cat.id}>{cat.name}</option>
                        ))}
                </select>

                <button className="add-product-button" onClick={handleAddProduct}>Produkt hinzufügen</button>
            </div>

            <ul className="product-list">
                {filteredProducts.map((product) => (
                    <li key={product.id} className="product-item">
                        <input
                            type="text"
                            value={product.name}
                            onChange={(e) => handleChange(product.id, "name", e.target.value)}
                        />
                        <textarea
                            value={product.description}
                            onChange={(e) => handleChange(product.id, "description", e.target.value)}
                            className="product-description"
                        />
                        <input
                            type="number"
                            value={product.price}
                            onChange={(e) => handleChange(product.id, "price", parseFloat(e.target.value))}
                        />
                        <input
                            type="number"
                            value={product.stock}
                            onChange={(e) => handleChange(product.id, "stock", parseInt(e.target.value))}
                        />

                        {/*  Bestehende Bilder des Produkts anzeigen */}
                        <div className="product-gallery">
                            {product.images && product.images.length > 0 ? (
                                product.images.map((image, index) => (
                                    <img
                                        key={index}
                                        src={image}
                                        alt={`${product.name} Bild ${index + 1}`}
                                        className="product-thumbnail"
                                        onClick={() => openImageInNewTab(image)}
                                    />
                                ))
                            ) : (
                                <p className="no-image-text">Kein Bild verfügbar</p>
                            )}
                        </div>

                        {/*  Mehrere Bilder hochladen */}
                        <input
                            type="file"
                            accept="image/*"
                            multiple
                            onChange={(e) => e.target.files && handleMultipleImageChange(product.id, e.target.files)}
                        />

                        <div className="button-group">
                            <button className="save-button" onClick={() => handleUpdate(product.id)}>Speichern</button>
                            <button className="delete-button" onClick={() => handleDelete(product.id)}>Löschen</button>
                        </div>
                    </li>
                ))}
            </ul>

        </div>
    );
}
