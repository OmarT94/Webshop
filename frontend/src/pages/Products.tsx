import { useEffect, useState } from "react";
import {getProducts, Product} from "../api/products";



export default function Products() {
    const [products, setProducts] = useState<Product[]>([]);

    useEffect(() => {
        async function fetchData() {
            const data = await getProducts();
            setProducts(data);
        }
        fetchData();
    }, []);

    return (
        <div className="p-6">
            <h2 className="text-2xl font-bold text-center mb-6">Produkte</h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                {products.map((product) => (
                    <div key={product.id} className="border rounded-lg p-4 shadow-lg flex flex-col items-center">
                        {product.imageBase64 && (
                            <img src={product.imageBase64} alt={product.name} className="w-40 h-40 object-cover"/>
                        )}
                        <h3 className="text-lg font-semibold mt-2">{product.name}</h3>
                        <p className="text-gray-500">{product.description}</p>
                        <p className="text-green-600 font-bold">{product.price} €</p>
                        <p className="text-gray-400">Lagerbestand: {product.stock}</p>
                    </div>
                ))}
            </div>
        </div>
    );
}
