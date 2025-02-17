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

    return (

        <div className="p-6">
            <h2 className="text-2xl font-bold text-center mb-6">Produkte</h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                {products.map((product) => (
                    <div key={product.id} className="border rounded-lg p-4 shadow-lg flex flex-col items-center">
                        <img src={product.imageBase64} alt={product.name} className="w-40 h-40 object-cover"/>
                        <h3 className="text-lg font-semibold mt-2">{product.name}</h3>
                        <p className="text-gray-500">{product.description}</p>
                        <p className="text-green-600 font-bold">{product.price} €</p>
                        <button
                            onClick={() => addItem(token!, userEmail!, {
                                productId: product.id,
                                name: product.name,
                                imageBase64: product.imageBase64,
                                quantity: 1,
                                price: product.price
                            })}
                            className="mt-2 p-2 bg-blue-500 text-white rounded"
                        >
                            🛒 In den Warenkorb
                        </button>
                    </div>
                ))}
            </div>
            <div
                className="p-6 text-xl"
                style={{
                    backgroundColor: "#3B82F6 !important",
                    color: "white !important",
                }}
            >
                🎯 Tailwind Test: Hintergrund muss jetzt blau sein!
            </div>


        </div>

    );
}
