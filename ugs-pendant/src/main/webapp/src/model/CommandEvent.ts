export type CommandEvent = {
    commandEventType: "COMMAND_COMPLETE" | "COMMAND_SENT",
    command: {
        command: string,
        response: string,
        isError: boolean,
        isOk: boolean
    }
};