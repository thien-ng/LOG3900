export interface IRegistration {
    username: string,
    password: string,
    firstName: string,
    lastName: string,
    avatar?: string,
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
    username:    string,
    firstName:   string,
    lastName:    string,
    connections: IConnection[],
    stats:       IStats,
    games:       IGame[],
    //avatar: Uint8Array,
}

export interface IConnection {
    username: string,
    isLogin: boolean,
    times: string,
}

export interface IGame {
    mode: string,
    date: string,
    players: IPlayer[],
}

export interface IModeDate {
    mode: string,
    date: string,
}

export interface IPlayer {
    username: string,
    score:    number
}

export interface IStats {
    totalGame:     number,
    winRate:       number,
    bestScore:     number,
    totalPlayTime: number,
    avgGameTime:   number,
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
