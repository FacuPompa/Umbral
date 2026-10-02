import { useEffect, useRef, useState } from 'react';
import { fetchJournalEntries } from './gameApi';

export function useJournalFeed(gameId, handle) {
  const [type, setType] = useState('');
  const [order, setOrder] = useState('DESC');
  const [revision, setRevision] = useState(0);
  const [feed, setFeed] = useState(null);
  const [loadingMoreKey, setLoadingMoreKey] = useState(null);
  const generation = useRef(0);
  const moreInFlight = useRef(false);
  const key = JSON.stringify([gameId, handle, type, order, revision]);
  const activeFeed = feed?.key === key ? feed : null;
  const entries = activeFeed?.items ?? [];
  const page = activeFeed?.page ?? 0;
  const hasNext = activeFeed?.hasNext ?? false;
  const error = activeFeed?.error ?? null;
  const loading = Boolean(gameId && handle && !activeFeed);
  const loadingMore = loadingMoreKey === key;

  useEffect(() => {
    const requestId = ++generation.current;
    moreInFlight.current = false;
    if (!gameId || !handle) return;

    fetchJournalEntries(gameId, { type, order })
      .then((response) => {
        if (generation.current !== requestId) return;
        setFeed({ ...response, key, error: null });
      })
      .catch((requestError) => {
        if (generation.current === requestId) {
          setFeed({ key, items: [], page: 0, hasNext: false, error: requestError.message });
        }
      });

    return () => { generation.current += 1; };
  }, [gameId, handle, type, order, revision, key]);

  async function loadMore() {
    if (!hasNext || loading || moreInFlight.current) return;
    const requestId = generation.current;
    moreInFlight.current = true;
    setLoadingMoreKey(key);
    setFeed((current) => ({ ...current, error: null }));
    try {
      const response = await fetchJournalEntries(gameId, { type, order, page: page + 1 });
      if (generation.current !== requestId) return;
      setFeed((current) => {
        const ids = new Set(current.items.map((entry) => entry.id));
        return { ...response, key, error: null,
          items: [...current.items, ...response.items.filter((entry) => !ids.has(entry.id))] };
      });
    } catch (requestError) {
      if (generation.current === requestId) {
        setFeed((current) => ({ ...current, error: requestError.message }));
      }
    } finally {
      if (generation.current === requestId) {
        moreInFlight.current = false;
        setLoadingMoreKey(null);
      }
    }
  }

  function refresh() {
    // Hide the old feed immediately, especially when progress moves backwards.
    generation.current += 1;
    setRevision((current) => current + 1);
  }

  return { entries, type, setType, order, setOrder, loading, loadingMore, error, hasNext, loadMore, refresh };
}
