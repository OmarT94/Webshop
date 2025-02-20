import { useState, useEffect } from "react";
import { useCartStore } from "../store/cartStore";
import { useAuthStore } from "../store/authStore";
import { checkout } from "../api/orders";
import { loadStripe } from "@stripe/stripe-js";
import { Elements, useStripe, useElements, CardElement, PaymentElement } from "@stripe/react-stripe-js";
import { useNavigate } from "react-router-dom";
import { getAddresses } from "../api/address";
import { AddressWithId } from "../api/orders.ts";

const stripeKey = import.meta.env.VITE_STRIPE_PUBLIC_KEY;
if (!stripeKey) {
    console.error(" Stripe Public Key fehlt! Stelle sicher, dass die .env Datei geladen wird.");
}

const stripePromise = loadStripe(stripeKey);

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

export default function Checkout() {
    const [clientSecret, setClientSecret] = useState<string | null>(null);
    const [paymentMethod, setPaymentMethod] = useState("card");
    const [loading, setLoading] = useState(true);
    const [addresses, setAddresses] = useState<AddressWithId[]>([]);
    const [selectedAddressId, setSelectedAddressId] = useState<string | null>(null);
    const [newAddress, setNewAddress] = useState({
        street: "",
        houseNumber: "",
        city: "",
        postalCode: "",
        country: "",
        telephoneNumber: "",
        isDefault: false
    });
    const [useNewAddress, setUseNewAddress] = useState(false);

    const userEmail = useAuthStore((state) => state.tokenEmail) ?? "";

    useEffect(() => {
        const fetchSecret = async () => {
            setClientSecret(null);
            setLoading(true);
            const secret = await fetchClientSecret(paymentMethod);
            setClientSecret(secret);
            setLoading(false);
        };
        fetchSecret();
    }, [paymentMethod]);

    useEffect(() => {
        if (userEmail) {
            getAddresses(userEmail)
                .then((data) => setAddresses(data))
                .catch((error) => console.error("Fehler beim Laden der Adressen:", error));
        }
    }, [userEmail]);

    return (
        <>
            {loading ? (
                <div className="p-6 text-center"> Lade Zahlungsmethoden...</div>
            ) : clientSecret ? (
                <Elements stripe={stripePromise} options={{ clientSecret }}>
                    <CheckoutForm
                        clientSecret={clientSecret}
                        paymentMethod={paymentMethod}
                        setPaymentMethod={setPaymentMethod}
                        addresses={addresses}
                        selectedAddressId={selectedAddressId}
                        setSelectedAddressId={setSelectedAddressId}
                        newAddress={newAddress}
                        setNewAddress={setNewAddress}
                        useNewAddress={useNewAddress}
                        setUseNewAddress={setUseNewAddress}
                    />
                </Elements>
            ) : (
                <div className="p-6 text-center text-red-500"> Fehler beim Laden der Zahlungsmethode</div>
            )}
        </>
    );
}

function CheckoutForm({
                          clientSecret,
                          paymentMethod,
                          setPaymentMethod,
                          addresses,
                          selectedAddressId,
                          setSelectedAddressId,
                          newAddress,
                          setNewAddress,
                          useNewAddress,
                          setUseNewAddress
                      }: {
    clientSecret: string;
    paymentMethod: string;
    setPaymentMethod: (method: string) => void;
    addresses: AddressWithId[];
    selectedAddressId: string | null;
    setSelectedAddressId: (id: string | null) => void;
    newAddress: any;
    setNewAddress: (address: any) => void;
    useNewAddress: boolean;
    setUseNewAddress: (use: boolean) => void;
}) {
    const stripe = useStripe();
    const elements = useElements();
    const navigate = useNavigate();

    const { token, firstName, lastName, tokenEmail: userEmail } = useAuthStore();
    const { fetchCart, clearCart } = useCartStore();

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (token && userEmail) {
            fetchCart(token, userEmail);
        }
    }, [token, userEmail, fetchCart]);

    const handlePayment = async () => {
        if (!stripe || !elements) return;
        setLoading(true);
        setError(null);

        if (!clientSecret) {
            console.error(" Kein Client Secret erhalten!");
            setError("Ein Fehler ist aufgetreten. Versuche es erneut.");
            setLoading(false);
            return;
        }

        const shippingAddress = useNewAddress
            ? newAddress
            : addresses.find((addr) => addr.id === selectedAddressId);

        if (!shippingAddress) {
            setError("Bitte wählen Sie eine Lieferadresse aus oder geben Sie eine neue Adresse ein.");
            setLoading(false);
            return;
        }

        try {
            let paymentResult;

            if (paymentMethod === "card") {
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
        <div className="checkout-container">
            <h2 className="checkout-title">🛒 Checkout</h2>

            <div className="checkout-section">
                <h3 className="checkout-subtitle">👤 Persönliche Daten</h3>
                <input type="text" value={firstName || ""} readOnly className="checkout-input readonly-input" />
                <input type="text" value={lastName || ""} readOnly className="checkout-input readonly-input" />
            </div>

            <div className="checkout-section">
                <h3 className="checkout-subtitle"> Lieferadresse</h3>
                <label>
                    <input
                        type="radio"
                        checked={!useNewAddress}
                        onChange={() => setUseNewAddress(false)}
                    />
                    Gespeicherte Adresse verwenden
                </label>
                <select className={"checkout-select"}
                    value={selectedAddressId || ""}
                    onChange={(e) => setSelectedAddressId(e.target.value)}
                    disabled={useNewAddress}
                >
                    <option value="">Adresse auswählen</option>
                    {addresses.map((address) => (
                        <option key={address.id} value={address.id}>
                            {address.street} {address.houseNumber}, {address.postalCode} {address.city}, {address.country}
                        </option>
                    ))}
                </select>

                <label>
                    <input className={"checkout-select"}
                        type="radio"
                        checked={useNewAddress}
                        onChange={() => setUseNewAddress(true)}
                    />
                    Neue Adresse verwenden
                </label>
                {useNewAddress && (
                    <div>
                        {["street", "houseNumber", "city", "postalCode", "country", "telephoneNumber"].map((field) => (
                            <input
                                key={field}
                                type="text"
                                placeholder={field}
                                value={newAddress[field]}
                                onChange={(e) => setNewAddress({ ...newAddress, [field]: e.target.value })}
                                className="checkout-input"
                            />
                        ))}
                    </div>
                )}
            </div>

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

            <div className="checkout-section">
                <h3 className="checkout-subtitle">💰 Zahlungsdetails</h3>
                {paymentMethod === "card" && <CardElement className="checkout-card" />}
                {paymentMethod === "sepa_debit" && (
                    <div>
                        <label className="checkout-label">IBAN</label>
                        <input type="text" placeholder="DE89 3704 0044 0532 0130 00" className="checkout-input" />
                    </div>
                )}
                {(paymentMethod === "klarna" || paymentMethod === "sofort") && <PaymentElement />}
            </div>

            <button onClick={handlePayment} className="checkout-button" disabled={loading || !stripe}>
                {loading ? "⏳ Zahlung läuft..." : "🛍 Jetzt kaufen"}
            </button>

            {error && <p className="checkout-error">{error}</p>}
        </div>
    );
}