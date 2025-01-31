import { useEffect, useState } from "react";
import { getProducts } from "../api/products";

type Product = {
    id: string;
    name: string;
    description: string;
    price: number;
};

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
        <div className="flex flex-col items-center">
            <h2 className="text-2xl font-bold">Produkte</h2>
            <ul className="mt-4">
                {products.map((product) => (
                    <li key={product.id} className="p-4 border rounded-lg shadow-lg mb-4">
                        <h3 className="text-xl font-semibold">{product.name}</h3>
                        <p>{product.description}</p>
                        <p className="text-green-600 font-bold">{product.price}€</p>
                    </li>
                ))}
            </ul>
        </div>
    );
}
