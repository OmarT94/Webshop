import { useState } from "react";
import { useAuthStore } from "../store/authStore";
import { login } from "../api/auth";
import { useNavigate } from "react-router-dom";

type LoginData = {
    email: string;
    password: string;
};

export default function Login() {
    const [formData, setFormData] = useState<LoginData>({ email: "", password: "" });
    const setToken = useAuthStore((state) => state.setToken);
    const navigate = useNavigate();

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            const data = await login(formData.email, formData.password);
            setToken(data.token);
            navigate("/"); // Nach Login zur Startseite zurück
        } catch (error) {
            alert("Login fehlgeschlagen!");
        }
    };

    return (
        <div className="flex flex-col items-center justify-center h-screen">
            <h2 className="text-2xl font-bold">Login</h2>
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
                <button type="submit">Login</button>
            </form>
        </div>
    );
}
