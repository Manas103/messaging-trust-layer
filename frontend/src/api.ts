export interface ReviewQueueEntry {
  id: number;
  messageId: string;
  status: "PENDING" | "APPROVED" | "DENIED";
  createdAtMillis: number;
  decidedAtMillis: number | null;
}

/** Thin wrapper over the Kotlin backend's two review-queue endpoints; every write here holds a message for a human decision, never auto-applies one. */
export class ReviewQueueApi {
  constructor(private readonly baseUrl: string) {}

  async fetchPending(): Promise<ReviewQueueEntry[]> {
    const response = await fetch(`${this.baseUrl}/review-queue`);
    if (!response.ok) {
      throw new Error(`failed to fetch review queue: ${response.status}`);
    }
    return response.json();
  }

  async decide(entryId: number, approve: boolean): Promise<ReviewQueueEntry> {
    const response = await fetch(`${this.baseUrl}/review-queue/${entryId}/decision`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ approve }),
    });
    if (!response.ok) {
      throw new Error(`failed to record decision: ${response.status}`);
    }
    return response.json();
  }
}
