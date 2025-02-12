import { useState, useEffect } from "react";
import { useCartStore } from "../store/cartStore";
import { useAuthStore } from "../store/authStore";
import { checkout } from "../api/orders";
import { loadStripe } from "@stripe/stripe-js";
import { Elements, useStripe, useElements, CardElement, PaymentElement } from "@stripe/react-stripe-js";
import { useNavigate } from "react-router-dom";

//  Stripe Public Key aus .env laden
const stripeKey = import.meta.env.VITE_STRIPE_PUBLIC_KEY;
if (!stripeKey) {
    console.error(" Stripe Public Key fehlt! Stelle sicher, dass die .env Datei geladen wird.");
}

const stripePromise = loadStripe(stripeKey);

// Client Secret abrufen
const fetchClientSecret = async (paymentMethod: string) => {
    const token = useAuthStore.getState().token;
    const totalPrice = useCartStore.getState().totalPrice;

    if (!token || totalPrice === 0) {
        console.error(" Kein Token oder totalPrice = 0!");
        return null;
    }

    try {
        const response = await fetch("/api/stripe/create-payment-intent", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`
            },
            body: JSON.stringify({
                amount: totalPrice * 100,
                currency: "eur",
                paymentMethodTypes: [paymentMethod]
            })
        });

        const data = await response.json();

        if (!data.clientSecret) {
            throw new Error(" Kein clientSecret erhalten!");
        }

        console.log(" Client Secret für", paymentMethod, "erhalten:", data.clientSecret);
        return data.clientSecret;
    } catch (error) {
        console.error(" Fehler beim Abrufen des Client Secrets:", error);
        return null;
    }
};


//  Checkout-Komponente mit `Elements`
export default function Checkout() {
    const [clientSecret, setClientSecret] = useState<string | null>(null);
    const [paymentMethod, setPaymentMethod] = useState("card");
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchSecret = async () => {
            setClientSecret(null);  // UI-Refresh forcieren
            setLoading(true);       // Ladezustand aktivieren
            const secret = await fetchClientSecret(paymentMethod);
            setClientSecret(secret);
            setLoading(false);       // Ladezustand deaktivieren
        };
        fetchSecret();
    }, [paymentMethod]);

    return (
        <>
            {loading ? (
                <div className="p-6 text-center"> Lade Zahlungsmethoden...</div>
            ) : clientSecret ? (
                <Elements stripe={stripePromise} options={{ clientSecret }}>
                    <CheckoutForm clientSecret={clientSecret} paymentMethod={paymentMethod} setPaymentMethod={setPaymentMethod} />
                </Elements>
            ) : (
                <div className="p-6 text-center text-red-500"> Fehler beim Laden der Zahlungsmethode</div>
            )}
        </>
    );
}

//  Checkout-Formular
function CheckoutForm({ clientSecret, paymentMethod, setPaymentMethod }: { clientSecret: string, paymentMethod: string, setPaymentMethod: (method: string) => void }) {
    const stripe = useStripe();
    const elements = useElements();
    const navigate = useNavigate();

    const { token } = useAuthStore();
    const userEmail = useAuthStore((state) => state.tokenEmail);
    const {  fetchCart, clearCart } = useCartStore();

    const [shippingAddress, setShippingAddress] = useState({
        street: "",
        city: "",
        postalCode: "",
        country: "",
    });

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (token && userEmail) {
            fetchCart(token, userEmail);
        }
    }, [token, userEmail, fetchCart]);

    //  Zahlung abwickeln
    const handlePayment = async () => {
        if (!stripe || !elements) return;
        setLoading(true);
        setError(null);

        try {
            let paymentResult;

            if (paymentMethod === "card") {
                //  Kreditkarte: Nutzt `CardElement`
                const cardElement = elements.getElement(CardElement);
                if (!cardElement) {
                    setError(" Fehler: Keine Kartendaten eingegeben.");
                    setLoading(false);
                    return;
                }

                paymentResult = await stripe.confirmCardPayment(clientSecret, {
                    payment_method: { card: cardElement },
                });

            } else {
                //  Alternative Zahlungsmethoden (Klarna, Sofort, SEPA)
                paymentResult = await stripe.confirmPayment({
                    elements,
                    confirmParams: { return_url: window.location.origin + "/profile" },
                });
            }

            if (paymentResult.error) {
                setError(paymentResult.error.message || " Zahlung fehlgeschlagen.");
                setLoading(false);
                return;
            }

            console.log(" Zahlung erfolgreich! PaymentIntent:", paymentResult.paymentIntent);

            if (paymentResult.paymentIntent?.id) {
                await checkout(token!, userEmail!, paymentResult.paymentIntent.id, paymentMethod, shippingAddress);
                clearCart(token!, userEmail!);
                navigate("/orders");
            } else {
                setError(" Zahlung fehlgeschlagen.");
            }
        } catch (err) {
            console.error(" Fehler bei der Zahlung:", err);
            setError("Ein Fehler ist aufgetreten.");
        } finally {
            setLoading(false);
        }
    };



    return (
        <div className="p-6">
            <h2 className="text-2xl font-bold">🛒 Checkout</h2>

            {/*  Lieferadresse */}
            <div className="mt-6 border p-4">
                <h3 className="font-semibold"> Lieferadresse</h3>
                {["street", "city", "postalCode", "country"].map((field) => (
                    <input
                        key={field}
                        type="text"
                        placeholder={field}
                        value={shippingAddress[field as keyof typeof shippingAddress]}
                        onChange={(e) =>
                            setShippingAddress({ ...shippingAddress, [field]: e.target.value })
                        }
                        className="w-full border p-2 mt-2"
                    />
                ))}
            </div>

            {/*  Zahlungsmethode */}
            <div className="mt-6 border p-4">
                <h3 className="font-semibold"> Zahlungsmethode</h3>
                <select
                    value={paymentMethod}
                    onChange={(e) => setPaymentMethod(e.target.value)}
                    className="w-full border p-2 mt-2"
                >
                    <option value="card">💳 Kreditkarte</option>
                    <option value="klarna">🔄 Klarna</option>
                    <option value="sofort">💶 Sofortüberweisung</option>
                    <option value="sepa_debit">🏦 SEPA-Lastschrift</option>
                </select>
            </div>

            {/*  PaymentElement für Klarna, Sofort, SEPA */}
            {/* 💰 Zahlungsdetails */}
            <div className="mt-6 border p-4">
                <h3 className="font-semibold">💰 Zahlungsdetails</h3>

                {/*  Kreditkarte → Nutzt `CardElement` */}
                {paymentMethod === "card" && <CardElement className="border p-2" />}

                {/*  SEPA-Lastschrift → IBAN-Feld hinzufügen */}
                {paymentMethod === "sepa_debit" && (
                    <div>
                        <label>IBAN</label>
                        <input type="text" placeholder="DE89 3704 0044 0532 0130 00" className="w-full border p-2 mt-2" />
                    </div>
                )}

                {/*  Klarna &  Sofort → Standard `PaymentElement` */}
                {(paymentMethod === "klarna" || paymentMethod === "sofort") && <PaymentElement />}
            </div>


            <button onClick={handlePayment} className="p-2 bg-green-500 text-white rounded" disabled={loading || !stripe}>
                {loading ? "⏳ Zahlung läuft..." : "🛍 Jetzt kaufen"}
            </button>

            {error && <p className="text-red-500 mt-4">{error}</p>}
        </div>
    );
}
