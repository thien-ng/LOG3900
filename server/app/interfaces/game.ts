import { IUser } from "./user-manager";

export interface IActiveLobby {
    users:      IUser[],
    private:    boolean,
    password?:  string,
    lobbyName:  string,
    size:       number,
}

export interface IJoinLobby {
    username:  string,
    private:   boolean,
    lobbyName: string,
    size?:      number,
    password?: string,
}

export interface ILeaveLobby {
    username:  string,
    lobbyName: string,
}

export interface IReceptMes {
    lobbyName: string,
    username: string,
    message: string,
}