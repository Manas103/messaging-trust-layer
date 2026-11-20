import { useCallback, useEffect, useState } from "react";
import { ReviewQueueApi, ReviewQueueEntry } from "../api";

/**
 * Every entry here is a message the backend CHALLENGED (top risk band): it is held, not
 * delivered, until a reviewer clicks Approve or Deny. There is no auto-approve path in this
 * component; a decision always comes from the one person looking at this screen.
 */
export function ReviewQueue({ api }: { api: ReviewQueueApi }) {
  const [entries, setEntries] = useState<ReviewQueueEntry[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<number | null>(null);

  const refresh = useCallback(async () => {
    try {
      const pending = await api.fetchPending();
      setEntries(pending);
      setError(null);
    } catch (e) {
      setError(e instanceof Error ? e.message : "unknown error");
    }
  }, [api]);

  useEffect(() => {
    refresh();
  }, [refresh]);

  async function decide(entryId: number, approve: boolean) {
    setBusyId(entryId);
    try {
      await api.decide(entryId, approve);
      setEntries((prev) => prev.filter((e) => e.id !== entryId));
    } catch (e) {
      setError(e instanceof Error ? e.message : "unknown error");
    } finally {
      setBusyId(null);
    }
  }

  return (
    <div>
      <h1>Held for review ({entries.length})</h1>
      {error && <p role="alert">{error}</p>}
      <ul>
        {entries.map((entry) => (
          <li key={entry.id} data-testid={`entry-${entry.id}`}>
            <span>message {entry.messageId}</span>
            <button disabled={busyId === entry.id} onClick={() => decide(entry.id, true)}>
              Approve
            </button>
            <button disabled={busyId === entry.id} onClick={() => decide(entry.id, false)}>
              Deny
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}
