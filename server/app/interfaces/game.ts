import { IUser } from "./user-manager";

export interface ILobby {
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