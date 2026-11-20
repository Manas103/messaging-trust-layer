import { createRoot } from "react-dom/client";
import { ReviewQueue } from "./components/ReviewQueue";
import { ReviewQueueApi } from "./api";

const api = new ReviewQueueApi(import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080");
const root = document.getElementById("root");
if (root) {
  createRoot(root).render(<ReviewQueue api={api} />);
}
