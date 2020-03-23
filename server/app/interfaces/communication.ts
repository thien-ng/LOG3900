import { GameMode } from "./game";

export interface IRegistration {
    username: string,
    password: string,
    firstName: string,
    lastName: string,
    //avatar: Uint8Array,
}

export interface ILogin {
    username: string,
    password: string,
}

export interface IStatus {
    status: number,
    message: string,
}

export interface IInfoUser {
    username: string,
    firstName: string,
    lastName: string,
    connections: IConnection[],
    //avatar: Uint8Array,
    //games: Igame[],
}

export interface IConnection {
    username: string,
    isLogin: boolean,
    times: string,
}

export interface Igame {
    time: string,
    players: { username: string, score: number }[],
}

export interface IInvitationChannel {
    message: string,
    channel: string,
}

export interface IInviteFriend {
    invitee: string,
    inviter: string,
    channel: string,
}


export interface IChannelParticipation {
    username: string,
    channel: string,
}
