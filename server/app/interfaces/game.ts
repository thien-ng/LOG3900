import { IUser } from "./user-manager";

/**
 * LOBBY
 */
export interface IGetLobby {
    usernames:  string[]
    private:    boolean,
    lobbyName:  string,
    size:       number,
    gameID:     string,
}

export interface IActiveLobby {
    users:      IUser[],
    private:    boolean,
    password?:  string,
    lobbyName:  string,
    size:       number,
    gameID:     string,
}

export interface IJoinLobby {
    username:  string,
    private:   boolean,
    lobbyName: string,
    size?:     number,
    gameID?:   string,
    password?: string,
}

export interface ILeaveLobby {
    username:  string,
    lobbyName: string,
}

export interface IReceptMesLob {
    lobbyName: string,
    username:  string,
    message:   string,
}

/**
 * LOBBY NOTIFICATIONS
 */
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

/**
 * GAMEPLAY
 */
export interface IGameplayChat {
    username:   string,
    content:    string,
}

export interface IDrawing {
    startPosX:      number,
    startPosY:      number,
    endPosX:        number,
    endPosY:        number,
    color:          number,
    width:          number,
}

export interface IGameplayDraw extends IDrawing{
    username:       string,
    // TODO add others
}

/**
 *  OTHER
 */
export enum GameMode {
    FFA,
    SprintSolo,
    SprintCollab,
}
