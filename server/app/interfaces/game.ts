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
    size?:     number,
    password?: string,
}

export interface ILeaveLobby {
    username:  string,
    lobbyName: string,
}

export interface IReceptMes {
    lobbyName: string,
    username:  string,
    message:   string,
}

export interface INotify {
    lobbyName: string,
    type:      LobbyNotif,
}

export interface INotifyUpdateUser extends INotify{
    user:      IUser;
}

export interface INotifyLobbyUpdate extends INotify {
    users?:     IUser[],
    private?:   boolean,
    size?:      number,
}

export enum LobbyNotif {
    join,
    leave,
    create,
    delete,
}

export interface IGameStartData {
    lobbyName: string,
    //Add data we want to start game with
}