import { IUser } from "./user-manager";

/**
 * LOBBY
 */
export interface IGetLobby {
    usernames:  string[]
    isPrivate:  boolean,
    lobbyName:  string,
    size:       number,
    mode:       GameMode,
}

export interface IActiveLobby {
    users:      IUser[],
    isPrivate:  boolean,
    password?:  string,
    lobbyName:  string,
    size:       number,
    mode:       GameMode,
}

export interface IJoinLobby {
    username:  string,
    isPrivate: boolean,
    lobbyName: string,
    size?:     number,
    mode?:     GameMode,
    password?: string,
}

export interface ILeaveLobby {
    username:  string,
    lobbyName: string,
}

export interface IReceptMesLob {
    lobbyName: string,
    username:  string,
    content:   string,
}

export interface ILobEmitMes extends IReceptMesLob{
    time: string,
}

/**
 * LOBBY NOTIFICATIONS
 */
export interface INotify {
    type:      LobbyNotif,
    lobbyName: string,
}

export interface INotifyUpdateUser extends INotify{
    user:      string;
}

export interface INotifyLobbyUpdate extends INotify {
    users?:     string[],
    isPrivate?: boolean,
    size?:      number,
}

export enum LobbyNotif {
    join   = "join",
    leave  = "leave",
    create = "create",
    delete = "delete",
}

/**
 * GAMEPLAY
 */
export interface IGameplayReady {
    username: string,
}

export interface IGameplayChat {
    username:   string,
    content:    string,
}

export interface IGameplayAnnouncement extends IGameplayChat{
    isServer:   boolean,
}

export interface IDrawing {
    startPosX:      number,
    startPosY:      number,
    endPosX:        number,
    endPosY:        number,
    color:          number,
    width:          number,
    idEnd:          number,
}

export interface IGameplayDraw extends IDrawing{
    username:       string,
    // TODO add others
}

export interface ICorrAns {
    username:  string,
    time:      number,
    ratio:     number,
}

/**
 * POINTS
 */
export interface IPoints {
    username: string,
    points:   number,
}

/**
 * PERSISTANCE
 */
export interface IGameInfo {
    type:   GameMode,
    date:   string,
    timer:  number,
    winner: string,
    users:  IUserPt[],
}

export interface IUserPt {
    username: string,
    point:    number,
}


/**
 *  OTHER
 */
export enum GameMode {
    FFA = "FFA",
    SOLO = "SOLO",
    COOP = "COOP",
}
