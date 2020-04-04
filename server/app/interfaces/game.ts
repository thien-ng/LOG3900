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
    whitelist?: IUser[],
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
    isKicked:  boolean,
}

export interface IReceptMesLob {
    lobbyName: string,
    username:  string,
    content:   string,
}

export enum Bot {
    humour = "bot:sebastien",
    kind   = "bot:olivia",
    mean   = "bot:olivier",
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
    username:      string;
}

export interface INotifyLobbyUpdate extends INotify {
    users?:     string[],
    isPrivate?: boolean,
    size?:      number,
    mode?:      GameMode,
}

export enum LobbyNotif {
    join   = "join",
    leave  = "leave",
    create = "create",
    delete = "delete",
    invitation = "invitation",
}

/**
 * GAMEPLAY
 */
export interface ICorrAns {
    username:  string,
    time:      number,
    ratio:     number,
}

export interface IDrawing {
    startPosX:      number,
    startPosY:      number,
    endPosX:        number,
    endPosY:        number,
    color:          number,
    width:          number,
    isEnd:          boolean,
    format:         Format,
    type:           Type,
}

export interface IEraser {
    type:       Type,
    x:          number,
    y:          number,
    width?:     number,
    eraser:     EraserType,
}

export interface IGameplayEraser extends IEraser {
    event:    EventType,
    username: string,
}

export interface IGameplayDraw extends IDrawing {
    event:    EventType,
    username: string,
}

export interface IGameplayReady {
    event:    EventType,
    username: string,
}

export interface IGameplayChat {
    event:    EventType,
    username: string,
    content:  string,
}

export interface IGameplayHint {
    event: EventType,
}

export interface IGameplayAnnouncement {
    username: string,
    content:  string,
    isServer: boolean,
}

export enum EventType {
    draw   = "draw",
    chat   = "chat",
    ready  = "ready",
    hint   = "hint",
}

export enum EraserType {
    point  = "point",
    stroke = "stroke",
}

export enum Format {
    circle = "circle",
    square = "square",
}

export enum Type {
    ink     = "ink",
    eraser  = "eraser",
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
