export interface IRegistration {
    username: string,
    password: string,
}

export interface ILogin {
    username: string,
    password: string,
}

export interface IStatus {
    status:  number;
    message: string;
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