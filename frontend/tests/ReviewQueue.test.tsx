import { render, screen, waitFor } from "@testing-library/react";
import { describe, expect, it, vi, beforeEach } from "vitest";
import { ReviewQueue } from "../src/components/ReviewQueue";
import { ReviewQueueApi } from "../src/api";

describe("ReviewQueue", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it("renders every pending entry fetched from the backend", async () => {
    const api = new ReviewQueueApi("http://test");
    vi.spyOn(api, "fetchPending").mockResolvedValue([
      { id: 1, messageId: "m1", status: "PENDING", createdAtMillis: 0, decidedAtMillis: null },
      { id: 2, messageId: "m2", status: "PENDING", createdAtMillis: 0, decidedAtMillis: null },
    ]);

    render(<ReviewQueue api={api} />);

    await waitFor(() => {
      expect(screen.getByTestId("entry-1")).toBeTruthy();
      expect(screen.getByTestId("entry-2")).toBeTruthy();
    });
  });

  it("approving an entry calls the backend and removes it from the list", async () => {
    const api = new ReviewQueueApi("http://test");
    vi.spyOn(api, "fetchPending").mockResolvedValue([
      { id: 1, messageId: "m1", status: "PENDING", createdAtMillis: 0, decidedAtMillis: null },
    ]);
    const decideSpy = vi.spyOn(api, "decide").mockResolvedValue({
      id: 1,
      messageId: "m1",
      status: "APPROVED",
      createdAtMillis: 0,
      decidedAtMillis: 123,
    });

    render(<ReviewQueue api={api} />);
    await waitFor(() => expect(screen.getByTestId("entry-1")).toBeTruthy());

    screen.getByText("Approve").click();

    await waitFor(() => expect(screen.queryByTestId("entry-1")).toBeNull());
    expect(decideSpy).toHaveBeenCalledWith(1, true);
  });

  it("shows an error if the backend call fails, without crashing", async () => {
    const api = new ReviewQueueApi("http://test");
    vi.spyOn(api, "fetchPending").mockRejectedValue(new Error("network down"));

    render(<ReviewQueue api={api} />);

    await waitFor(() => expect(screen.getByRole("alert")).toBeTruthy());
  });
});
