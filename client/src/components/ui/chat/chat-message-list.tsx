// INFO: Modifiec according to https://github.com/jakobhoeg/shadcn-chat/issues/66

import * as React from "react";
import { ArrowDown } from "lucide-react";
import { Button } from "@/components/ui/button";
import { ScrollArea } from "@/components/ui/scroll-area";
import {useAutoScroll} from "@/components/ui/chat/hooks/useAutoScroll";

interface ChatMessageListProps extends React.HTMLAttributes<HTMLDivElement> {
    smooth?: boolean;
}

const ChatMessageList = React.forwardRef<HTMLDivElement, ChatMessageListProps>(
    ({ className, children, smooth = true, ...props }, _ref) => {
        const {
            containerRef,
            isAtBottom,
            autoScrollEnabled,
            scrollToBottom,
            disableAutoScroll,
        } = useAutoScroll({
            smooth,
            content: children,
        });

        return (
            <div className="relative w-full h-full" ref={containerRef}>
                <ScrollArea className={`w-full h-full ${className}`}>
                    <div className="flex flex-col gap-2 p-4">{children}</div>
                </ScrollArea>
                {!isAtBottom && (
                    <Button
                        onClick={scrollToBottom}
                        size="icon"
                        variant="outline"
                        className="absolute bottom-2 left-1/2 transform -translate-x-1/2 inline-flex rounded-full shadow-md"
                        aria-label="Scroll to bottom"
                    >
                        <ArrowDown className="h-4 w-4" />
                    </Button>
                )}
            </div>
        );
    }
);

ChatMessageList.displayName = "ChatMessageList";

export { ChatMessageList };
