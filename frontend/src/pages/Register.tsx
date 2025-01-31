import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { register } from "../api/auth";

type RegisterData = {
    email: string;
    password: string;
    role: string;
};

export default function Register() {
    const [formData, setFormData] = useState<RegisterData>({
        email: "",
        password: "",
        role: "ROLE_USER",
    });

    const navigate = useNavigate();

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            await register(formData.email, formData.password, formData.role);
            localStorage.setItem("registered", "true"); // Registrierungsstatus speichern
            alert("Registrierung erfolgreich!");
            navigate("/login"); // Zur Login-Seite weiterleiten
        } catch (error) {
            alert("Fehler bei der Registrierung!");
        }
    };

    return (
        <div className="flex flex-col items-center justify-center h-screen">
            <h2 className="text-2xl font-bold">Registrieren</h2>
            <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                <input
                    type="email"
                    name="email"
                    placeholder="E-Mail"
                    value={formData.email}
                    onChange={handleChange}
                    required
                />
                <input
                    type="password"
                    name="password"
                    placeholder="Passwort"
                    value={formData.password}
                    onChange={handleChange}
                    required
                />
                <select name="role" value={formData.role} onChange={handleChange}>
                    <option value="ROLE_USER">Benutzer</option>
                    <option value="ROLE_ADMIN">Admin</option>
                </select>
                <button type="submit">Registrieren</button>
            </form>
        </div>
    );
}
