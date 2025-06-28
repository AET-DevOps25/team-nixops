// @hidden
// INFO: Modifiec according to https://github.com/jakobhoeg/shadcn-chat/issues/66

import { useCallback, useEffect, useRef, useState } from "react";

interface ScrollState {
  isAtBottom: boolean;
  autoScrollEnabled: boolean;
}

interface UseAutoScrollOptions {
  offset?: number;
  smooth?: boolean;
  content?: React.ReactNode;
}

export function useAutoScroll(options: UseAutoScrollOptions = {}) {
  const { offset = 20, smooth = false, content } = options;
  const containerRef = useRef<HTMLDivElement>(null);
  const lastContentHeight = useRef(0);
  const userHasScrolled = useRef(false);

  const [scrollState, setScrollState] = useState<ScrollState>({
    isAtBottom: true,
    autoScrollEnabled: true,
  });

  // Helper to get the viewport element
  const getViewportElement = useCallback((): HTMLElement | null => {
    if (!containerRef.current) return null;
    return containerRef.current.querySelector('[data-radix-scroll-area-viewport]');
  }, []);

  const checkIsAtBottom = useCallback(
      (element: HTMLElement) => {
        const { scrollTop, scrollHeight, clientHeight } = element;
        const distanceToBottom = Math.abs(
            scrollHeight - scrollTop - clientHeight
        );
        return distanceToBottom <= offset;
      },
      [offset]
  );

  const scrollToBottom = useCallback(
      (instant?: boolean) => {
        const viewportElement = getViewportElement();
        if (!viewportElement) return;

        const targetScrollTop =
            viewportElement.scrollHeight - viewportElement.clientHeight;

        if (instant) {
          viewportElement.scrollTop = targetScrollTop;
        } else {
          viewportElement.scrollTo({
            top: targetScrollTop,
            behavior: smooth ? "smooth" : "auto",
          });
        }

        setScrollState({
          isAtBottom: true,
          autoScrollEnabled: true,
        });
        userHasScrolled.current = false;
      },
      [smooth, getViewportElement]
  );

  const handleScroll = useCallback(() => {
    const viewportElement = getViewportElement();
    if (!viewportElement) return;

    const atBottom = checkIsAtBottom(viewportElement);

    setScrollState((prev) => ({
      isAtBottom: atBottom,
      // Re-enable auto-scroll if at the bottom
      autoScrollEnabled: atBottom ? true : prev.autoScrollEnabled,
    }));
  }, [checkIsAtBottom, getViewportElement]);

  useEffect(() => {
    const viewportElement = getViewportElement();
    if (!viewportElement) return;

    viewportElement.addEventListener("scroll", handleScroll, { passive: true });
    return () => viewportElement.removeEventListener("scroll", handleScroll);
  }, [handleScroll, getViewportElement]);

  useEffect(() => {
    const viewportElement = getViewportElement();
    if (!viewportElement) return;

    const currentHeight = viewportElement.scrollHeight;
    const hasNewContent = currentHeight !== lastContentHeight.current;

    if (hasNewContent) {
      if (scrollState.autoScrollEnabled) {
        requestAnimationFrame(() => {
          scrollToBottom(lastContentHeight.current === 0);
        });
      }
      lastContentHeight.current = currentHeight;
    }
  }, [content, scrollState.autoScrollEnabled, scrollToBottom, getViewportElement]);

  useEffect(() => {
    const viewportElement = getViewportElement();
    if (!viewportElement) return;

    const resizeObserver = new ResizeObserver(() => {
      if (scrollState.autoScrollEnabled) {
        scrollToBottom(true);
      }
    });

    resizeObserver.observe(viewportElement);
    return () => resizeObserver.disconnect();
  }, [scrollState.autoScrollEnabled, scrollToBottom, getViewportElement]);

  const disableAutoScroll = useCallback(() => {
    const viewportElement = getViewportElement();
    const atBottom = viewportElement ? checkIsAtBottom(viewportElement) : false;

    // Only disable if not at bottom
    if (!atBottom) {
      userHasScrolled.current = true;
      setScrollState((prev) => ({
        ...prev,
        autoScrollEnabled: false,
      }));
    }
  }, [checkIsAtBottom, getViewportElement]);

  return {
    containerRef,
    isAtBottom: scrollState.isAtBottom,
    autoScrollEnabled: scrollState.autoScrollEnabled,
    scrollToBottom: () => scrollToBottom(false),
    disableAutoScroll,
  };
}
