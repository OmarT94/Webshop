import { useEffect, useState } from "react";
import { getAddresses, addAddress, updateAddress, deleteAddress } from "../api/address";
import { useAuthStore } from "../store/authStore";
import { Address } from "../api/orders.ts";

export default function Addresses() {
    const userEmail = useAuthStore((state) => state.tokenEmail) ?? "";
    const [addresses, setAddresses] = useState<Address[]>([]);
    const [editingAddressId, setEditingAddressId] = useState<string | null>(null);
    const [editedAddress, setEditedAddress] = useState<Address | null>(null);
    const [newAddress, setNewAddress] = useState<Address>({
        id: "",
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
                .then((data) => setAddresses(data))
                .catch((error) => console.error(" Fehler beim Laden der Adressen:", error));
        }
    }, [userEmail]);


    const handleAddAddress = async () => {
        if (!userEmail) return;
        try {
            const addedAddress = await addAddress(userEmail, newAddress);

            setAddresses((prev) => [...prev, addedAddress]); //  UI sofort aktualisieren
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

            //  **Zusätzlicher Sicherheitsschritt**: Aktualisierte Adressen nochmal abrufen
            const updatedList = await getAddresses(userEmail);
            setAddresses(updatedList);

        } catch (error) {
            console.error(" Fehler beim Hinzufügen der Adresse:", error);
        }
    };

    const handleEditAddress = (address: Address) => {
        setEditingAddressId(address.id);
        setEditedAddress({ ...address });
    };

    const handleSaveAddress = async (id: string) => {
        if (!userEmail || !editedAddress) return;
        try {
            const updated = await updateAddress(userEmail, id, editedAddress);

            setAddresses((prev) =>
                prev.map((addr) => (addr.id === id ? { ...addr, ...updated } : addr)) //  UI sofort aktualisieren
            );

            setEditingAddressId(null);
            setEditedAddress(null);

            //  **Sicherheitsschritt**: Aktualisierte Adressen nochmal abrufen
            const updatedList = await getAddresses(userEmail);
            setAddresses(updatedList);

        } catch (error) {
            console.error("Fehler beim Aktualisieren der Adresse:", error);
        }
    };


    const handleDeleteAddress = async (id: string) => {
        if (!userEmail) return;
        try {
            await deleteAddress(userEmail, id);
            setAddresses(addresses.filter((addr) => addr.id !== id));
        } catch (error) {
            console.error(" Fehler beim Löschen der Adresse:", error);
        }
    };

    return (
        <div className="address-container">
            <h2 className="address-title">📍 Meine Adressen</h2>

            {/*  Neue Adresse hinzufügen */}
            <div className="address-form">
                <h3>➕ Adresse hinzufügen</h3>
                {["street", "houseNumber", "city", "postalCode", "country", "telephoneNumber"].map((field) => (
                    <input
                        key={field}
                        type="text"
                        placeholder={field}
                        value={String(newAddress[field as keyof Address])} //  Fix: String-Wert erzwingen
                        onChange={(e) => setNewAddress({ ...newAddress, [field]: e.target.value })}
                        className="address-input"
                    />
                ))}
                <label className="address-checkbox">
                    <input
                        type="checkbox"
                        checked={newAddress.isDefault}
                        onChange={(e) => setNewAddress({ ...newAddress, isDefault: e.target.checked })}
                    />
                    Als Standardadresse setzen
                </label>
                <button className="address-add-button" onClick={handleAddAddress}>
                    ➕ Adresse hinzufügen
                </button>
            </div>

            {/*  Adressliste */}
            <ul className="address-list">
                {addresses.length === 0 ? (
                    <p className="no-address-text"> Keine Adresse gefunden.</p>
                ) : (
                    addresses.map((address, index) => (
                        <li key={address.id || `address-${index}`} className="address-item">
                            <h3 className="address-title"> Adresse {index + 1}</h3>

                            {editingAddressId === address.id ? (
                                <div className="edit-mode">
                                    {["street", "houseNumber", "postalCode", "city", "country"].map((field) => (
                                        <input
                                            key={field}
                                            type="text"
                                            value={String(editedAddress?.[field as keyof Address] || "")} //  Fix: Sicherstellen, dass immer ein String übergeben wird
                                            onChange={(e) => setEditedAddress({ ...editedAddress!, [field]: e.target.value })}
                                            className="address-input"
                                        />
                                    ))}
                                    <div className="address-buttons">
                                        <button className="save-button" onClick={() => handleSaveAddress(address.id)}>
                                            ✅ Speichern
                                        </button>
                                        <button className="cancel-button" onClick={() => setEditingAddressId(null)}>
                                            ❌ Abbrechen
                                        </button>
                                    </div>
                                </div>
                            ) : (
                                <>
                                    <p><strong>Straße:</strong> {address.street}</p>
                                    <p><strong>Hausnummer:</strong> {address.houseNumber}</p>
                                    <p><strong>PLZ & Stadt:</strong> {address.postalCode} {address.city}</p>
                                    <p><strong>Land:</strong> {address.country}</p>
                                    <p><strong>📞 Telefon:</strong> {address.telephoneNumber}</p>
                                    <p><strong>🌟 Standardadresse:</strong> {address.isDefault ? "✅ Ja" : "❌ Nein"}</p>

                                    <div className="address-buttons">
                                        <button className="edit-button" onClick={() => handleEditAddress(address)}>
                                            ✏️ Bearbeiten
                                        </button>
                                        <button className="delete-button" onClick={() => handleDeleteAddress(address.id)}>
                                            🗑 Löschen
                                        </button>
                                    </div>
                                </>
                            )}
                        </li>
                    ))
                )}
            </ul>
        </div>
    );
}
