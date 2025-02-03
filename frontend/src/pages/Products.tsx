import { useEffect, useState } from "react";
import { getProducts } from "../api/products";

type Product = {
    id: string;
    name: string;
    description: string;
    price: number;
    stock: number;
    image: string;
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
            <ul>
                {products.map((product) => (
                    <li key={product.id}> {/* Nutze die eindeutige ID als Key */}
                        <div>{product.name}</div>
                        <div>{product.description}</div>
                        <div>{product.price}€</div>
                    </li>
                ))}
            </ul>

        </div>
    );
}
