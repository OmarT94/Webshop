import { useState, useEffect } from "react";
import { useCartStore } from "../store/cartStore";
import { useAuthStore } from "../store/authStore";
import { checkout } from "../api/orders";
import { loadStripe } from "@stripe/stripe-js";
import { Elements, useStripe, useElements, CardElement, PaymentElement } from "@stripe/react-stripe-js";
import { useNavigate } from "react-router-dom";


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

    const { token, firstName, lastName, tokenEmail: userEmail } = useAuthStore();
    const {fetchCart, clearCart} = useCartStore();

    const [shippingAddress, setShippingAddress] = useState({

        street: "",
        houseNumber: "",
        city: "",
        postalCode: "",
        country: "",
        telephoneNumber: "",
        isDefault: false,
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
        //  PRÜFE, OB CLIENT SECRET EXISTIERT!**
        if (!clientSecret) {
            console.error(" Kein Client Secret erhalten!");
            setError("Ein Fehler ist aufgetreten. Versuche es erneut.");
            setLoading(false);
            return;
        }

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
                    payment_method: {card: cardElement},
                });

            } else {
                //  Alternative Zahlungsmethoden (Klarna, Sofort, SEPA)
                paymentResult = await stripe.confirmPayment({
                    elements,
                    confirmParams: {return_url: window.location.origin + "/profile"},
                });
            }

            if (paymentResult.error) {
                setError(paymentResult.error.message || " Zahlung fehlgeschlagen.");
                setLoading(false);
                return;
            }

            console.log(" Zahlung erfolgreich! PaymentIntent:", paymentResult.paymentIntent);

            if (paymentResult.paymentIntent?.id) {
                console.log(" Versandadresse wird gesendet:", shippingAddress);
                //  **VALIDIERUNG: Stelle sicher, dass alle Felder von shippingAddress existieren!**
                if (

                    !shippingAddress.street.trim() ||
                    !shippingAddress.houseNumber.trim() ||
                    !shippingAddress.city.trim() ||
                    !shippingAddress.postalCode.trim() ||
                    !shippingAddress.country.trim() ||
                    !shippingAddress.telephoneNumber.trim()
                ) {
                    console.error(" Fehler: Ungültige Lieferadresse!", shippingAddress);
                    setError(" Ungültige Lieferadresse! Bitte alle Felder ausfüllen.");
                    setLoading(false);
                    return;
                }
                console.log(" Sende Checkout-Daten:", {
                    token,
                    userEmail,
                    paymentIntentId: paymentResult.paymentIntent?.id,
                    paymentMethod,
                    shippingAddress
                });

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
        <div className="checkout-container">
            <h2 className="checkout-title">🛒 Checkout</h2>

            {/* Vor- und Nachname anzeigen */}
            <div className="checkout-section">
                <h3 className="checkout-subtitle">👤 Persönliche Daten</h3>
                <input type="text" value={firstName || ""} readOnly className="checkout-input readonly-input"/>
                <input type="text" value={lastName || ""} readOnly className="checkout-input readonly-input"/>
            </div>

            {/*  Lieferadresse */}
            <div className="checkout-section">
                <h3 className="checkout-subtitle"> Lieferadresse</h3>
                {["street", "houseNumber", "city", "postalCode", "country", "telephoneNumber"].map((field) => (
                    <input
                        key={field}
                        type="text"
                        placeholder={field}
                        value={
                            shippingAddress[field as keyof typeof shippingAddress] !== undefined &&
                            shippingAddress[field as keyof typeof shippingAddress] !== null
                                ? String(shippingAddress[field as keyof typeof shippingAddress])
                                : ""
                        }
                        onChange={(e) =>
                            setShippingAddress({...shippingAddress, [field]: e.target.value})
                        }
                        className="checkout-input"
                    />
                ))}
            </div>


            {/*  Zahlungsmethode */}
            <div className="checkout-section">
                <h3 className="checkout-subtitle"> Zahlungsmethode</h3>
                <select
                    value={paymentMethod}
                    onChange={(e) => setPaymentMethod(e.target.value)}
                    className="checkout-select"
                >
                    <option value="card">💳 Kreditkarte</option>
                    <option value="klarna">🔄 Klarna</option>
                    <option value="sofort">💶 Sofortüberweisung</option>
                    <option value="sepa_debit">🏦 SEPA-Lastschrift</option>
                </select>
            </div>

            {/*  Zahlungsdetails */}
            <div className="checkout-section">
                <h3 className="checkout-subtitle">💰 Zahlungsdetails</h3>

                {paymentMethod === "card" && <CardElement className="checkout-card"/>}
                {paymentMethod === "sepa_debit" && (
                    <div>
                        <label className="checkout-label">IBAN</label>
                        <input type="text" placeholder="DE89 3704 0044 0532 0130 00" className="checkout-input"/>
                    </div>
                )}
                {(paymentMethod === "klarna" || paymentMethod === "sofort") && <PaymentElement/>}
            </div>

            {/* 🛍️ Kaufen Button */}
            <button onClick={handlePayment} className="checkout-button" disabled={loading || !stripe}>
                {loading ? "⏳ Zahlung läuft..." : "🛍 Jetzt kaufen"}
            </button>

            {error && <p className="checkout-error">{error}</p>}
        </div>
    );
}
