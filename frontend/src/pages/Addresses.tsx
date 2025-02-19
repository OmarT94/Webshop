import { useEffect, useState } from "react";
import { getAddresses, addAddress, updateAddress, deleteAddress } from "../api/address";
import { useAuthStore } from "../store/authStore";
import { Address } from "../api/orders.ts";

export default function Addresses() {
    const userEmail = useAuthStore((state) => state.tokenEmail) ?? "";
    const [addresses, setAddresses] = useState<Address[]>([]);
    const [newAddress, setNewAddress] = useState<Address>({
        id: "", //  `id` hinzugefügt
        street: "",
        houseNumber: "",
        city: "",
        postalCode: "",
        country: "",
        telephoneNumber: "",
        isDefault: false
    });

    useEffect(() => {
        if (userEmail) {
            getAddresses(userEmail)
                .then((data) => {
                    console.log(" Geladene Adressen:", data);
                    setAddresses(data);
                })
                .catch((error) => console.error(" Fehler beim Laden der Adressen:", error));
        }
    }, [userEmail]);

    const handleAddAddress = async () => {
        if (!userEmail) {
            console.error("Kein Benutzer angemeldet!");
            return;
        }

        // **Validierung: Stelle sicher, dass alle Felder ausgefüllt sind**
        if (!newAddress.street.trim() || !newAddress.city.trim() || !newAddress.postalCode.trim() || !newAddress.country.trim()) {
            console.error("⚠️ Bitte alle erforderlichen Felder ausfüllen!");
            alert("⚠️ Bitte alle Felder ausfüllen!"); // Optional: UI-Warnung
            return;
        }

        try {
            console.log(" Sende folgende Adresse ans Backend:", newAddress);
            const addedAddress = await addAddress(userEmail, newAddress);

            console.log(" Adresse erfolgreich gespeichert:", addedAddress);

            // **UI sofort aktualisieren!**
            setAddresses((prevAddresses) => [...prevAddresses, addedAddress]);

            // **🛠 Fix für das Zurücksetzen:**
            setNewAddress({
                id: "",
                street: "",
                houseNumber: "",
                city: "",
                postalCode: "",
                country: "",
                telephoneNumber: "",
                isDefault: false
            });

            // ** Neue Adressen sofort abrufen!**
            getAddresses(userEmail)
                .then((data) => {
                    console.log(" Aktualisierte Adressen nach Hinzufügen:", data);
                    setAddresses(data);
                })
                .catch((error) => console.error(" Fehler beim erneuten Laden der Adressen:", error));

        } catch (error: any) {
            console.error(" Fehler beim Hinzufügen der Adresse:", error.response?.data || error.message);
        }
    };



    const handleUpdateAddress = async (id: string) => {
        if (!userEmail) return;

        try {
            console.log(" Sende Update für Adresse:", id);
            const updatedAddress = addresses.find((addr) => addr.id === id);
            if (!updatedAddress) {
                console.error(" Keine passende Adresse gefunden!");
                return;
            }

            const result = await updateAddress(userEmail, id, updatedAddress);

            //  **Sofortige Aktualisierung in der Liste**
            setAddresses((prev) => prev.map((addr) => (addr.id === id ? result : addr)));

            console.log(" Adresse erfolgreich aktualisiert:", result);
        } catch (error) {
            console.error(" Fehler beim Aktualisieren der Adresse:", error);
        }
    };


    const handleDeleteAddress = async (id?: any) => {
        console.log(" Debug: ID beim Löschen:", id);

        if (!id) {
            console.error(" Ungültige Address-ID: ID ist undefined oder null!");
            return;
        }

        if (typeof id === "object") {
            console.error(" ID ist ein Objekt! Inhalt:", JSON.stringify(id, null, 2));
        } else {
            console.log(" ID ist ein String:", id);
        }

        // Falls `id` ein Objekt ist, versuche `id.$oid` oder `id.id` zu extrahieren:
        const addressId =
            typeof id === "object" && id.$oid ? id.$oid :
                typeof id === "object" && id.id ? id.id :
                    typeof id === "string" ? id :
                        "";

        console.log("🗑 Verwendete Address-ID für DELETE:", addressId);

        if (!userEmail) {
            console.error(" Kein Benutzer-E-Mail gefunden!");
            return;
        }

        if (!addressId || addressId.trim() === "") {
            console.error(" Fehler: Address-ID ist leer!");
            return;
        }

        try {
            console.log(` DELETE Anfrage an: /api/users/${userEmail}/addresses/${addressId}`);
            await deleteAddress(userEmail, addressId);
            setAddresses((prev) => prev.filter((addr) => addr.id !== addressId));
            console.log(" Adresse erfolgreich gelöscht!");
        } catch (error: any) {
            console.error(" Fehler beim Löschen der Adresse:", error.response?.data || error.message);
        }
    };

    return (
        <div className="address-container">
            <h2 className="address-title">📍 Meine Adressen</h2>

            <div className="address-form">
                <h3>Neue Adresse hinzufügen</h3>
                {["street", "houseNumber", "city", "postalCode", "country", "telephoneNumber"].map((field) => (
                    <input
                        key={field}
                        type="text"
                        placeholder={field}
                        value={newAddress[field as keyof Address]}
                        onChange={(e) => setNewAddress({...newAddress, [field]: e.target.value})}
                        className="address-input"
                    />
                ))}
                <label>
                    <input
                        type="checkbox"
                        checked={newAddress.isDefault}
                        onChange={(e) => setNewAddress({...newAddress, isDefault: e.target.checked})}
                    />
                    Als Standardadresse setzen
                </label>
                <button className="address-add-button" onClick={handleAddAddress}>
                    Adresse hinzufügen
                </button>
            </div>

            <ul className="address-list">
                {addresses.length === 0 ? (
                    <p className="no-address-text">❌ Keine Adresse gefunden.</p>
                ) : (
                    addresses.map((address, index) => {
                        console.log(" Adresse aus API:", address); // **DEBUG: Zeigt geladene Adresse an!**

                        return (
                            <li key={address.id || `address-${index}`} className="address-item">
                                <h3 className="address-title">🏠 Adresse {index + 1}</h3>

                                {/*  Falls `street` nicht existiert, zeige eine Fehlermeldung */}
                                <p className="address-detail">
                                    <strong>Straße:</strong> {address.street ? address.street : "⚠️ Keine Straße angegeben"}
                                </p>

                                <p className="address-detail">
                                    <strong>Hausnummer:</strong> {address.houseNumber || "⚠️ Keine Hausnummer"}
                                </p>

                                <p className="address-detail">
                                    <strong>PLZ &
                                        Stadt:</strong> {address.postalCode || "⚠️ Keine Postleitzahl"} {address.city || "⚠️ Keine Stadt"}
                                </p>

                                <p className="address-detail">
                                    <strong>Land:</strong> {address.country || "⚠️ Kein Land angegeben"}
                                </p>

                                <p className="address-detail">
                                    <strong> Telefon:</strong> {address.telephoneNumber || "⚠️ Keine Telefonnummer"}
                                </p>

                                <p className="address-detail">
                                    <strong> Standardadresse:</strong> {address.isDefault ? "✅ Ja" : "❌ Nein"}
                                </p>

                                {/* 🛠 Buttons für Bearbeiten & Löschen */}
                                <div className="address-buttons">
                                    <button className="address-update-button"
                                            onClick={() => handleUpdateAddress(address.id)}>
                                        ✏️ Bearbeiten
                                    </button>
                                    <button
                                        className="address-delete-button"
                                        onClick={() => {
                                            if (!address.id) {
                                                console.error("Kein ID gefunden für Adresse:", address);
                                                return;
                                            }
                                            handleDeleteAddress(address.id);
                                        }}
                                    >
                                        🗑 Löschen
                                    </button>
                                </div>
                            </li>
                        );
                    })
                )}
            </ul>

        </div>
    );
}
