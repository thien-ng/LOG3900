import { IUser } from "./user-manager";

export interface IActiveLobby {
    users:      IUser[],
    private:    boolean,
    password?:  string,
}

export interface IJoinLobby {
    username:  string,
    private:   boolean,
    lobbyName: string,
    password?: string,
}

export interface ILeaveLobby {
    username:  string,
    lobbyName: string,
}