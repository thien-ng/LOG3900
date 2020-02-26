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

export interface IinfoUser {
    username: string,
    first_name: string,
    last_name: string,
    //avatar: Uint8Array,
    //connections: Iconnection[],
    //games: Igame[],

}

export interface Igame {
    time: string,
    players: { username: string, score: number }[],
}

export interface Iconnection {
    date: string,
    is_login: boolean,
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
