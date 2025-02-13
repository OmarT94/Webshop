import { useState, useEffect } from "react";
import { getAllOrders, searchOrdersByEmail, searchOrdersByStatus, searchOrdersByPaymentStatus } from "../api/orders";

export default function AdminOrderSearch() {
    const [email, setEmail] = useState("");
    const [status, setStatus] = useState("");
    const [paymentStatus, setPaymentStatus] = useState("");
    const [orders, setOrders] = useState<any[]>([]);

    useEffect(() => {
        document.title = "Admin Bestellungen";
        async function fetchOrders() {
            const data = await getAllOrders();
            setOrders(data);
        }
        fetchOrders();
    }, []);

    const handleEmailSearch = async () => {
        if (!email.trim()) return;
        const data = await searchOrdersByEmail(email);
        setOrders(data);
    };

    const handleStatusSearch = async () => {
        if (!status.trim()) return;
        const data = await searchOrdersByStatus(status);
        setOrders(data);
    };

    const handlePaymentStatusSearch = async () => {
        if (!paymentStatus.trim()) return;
        const data = await searchOrdersByPaymentStatus(paymentStatus);
        setOrders(data);
    };

    return (
        <div className="p-6">
            <h2 className="text-2xl font-bold text-center">🔍 Bestellsuche (Admin)</h2>

            <div className="mt-4 flex flex-col gap-4 max-w-md mx-auto">
                <div>
                    <input
                        type="text"
                        placeholder="Benutzer-E-Mail"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        className="border p-2 w-full"
                    />
                    <button onClick={handleEmailSearch} className="bg-blue-500 text-white p-2 w-full mt-2">Suchen</button>
                </div>

                <div>
                    <input
                        type="text"
                        placeholder="Bestellstatus"
                        value={status}
                        onChange={(e) => setStatus(e.target.value)}
                        className="border p-2 w-full"
                    />
                    <button onClick={handleStatusSearch} className="bg-green-500 text-white p-2 w-full mt-2">Suchen</button>
                </div>

                <div>
                    <input
                        type="text"
                        placeholder="Zahlungsstatus"
                        value={paymentStatus}
                        onChange={(e) => setPaymentStatus(e.target.value)}
                        className="border p-2 w-full"
                    />
                    <button onClick={handlePaymentStatusSearch} className="bg-purple-500 text-white p-2 w-full mt-2">Suchen</button>
                </div>
            </div>

            <div className="mt-6 max-w-lg mx-auto">
                <h3 className="text-xl font-bold text-center">📋 Bestellungen</h3>
                <ul className="mt-4 border p-4 rounded-lg">
                    {orders.length > 0 ? (
                        orders.map((order) => (
                            <li key={order.id} className="border-b p-2 flex flex-col gap-2">
                                <p><strong>Kunde:</strong> {order.userEmail}</p>
                                <p><strong>Status:</strong> {order.orderStatus}</p>
                                <p><strong>Zahlungsstatus:</strong> {order.paymentStatus}</p>
                                <p><strong>Gesamtpreis:</strong> {order.totalPrice} €</p>
                            </li>
                        ))
                    ) : (
                        <p className="text-center text-gray-500">Keine Bestellungen gefunden</p>
                    )}
                </ul>
            </div>
        </div>
    );
}
