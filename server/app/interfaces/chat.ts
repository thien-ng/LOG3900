interface IMessage {
    content:    string,
    time:       string,
}

export interface IChannelMessageDB extends IMessage {
    channel_id: number,
    account_id: number,
}

export interface IChannelMessageReq extends IMessage {
    username: string,
}

export interface IReceptMes {
    username:   string,
    channel_id: number,
    content:    string,
}

export interface IEmitMes extends IReceptMes {
    time: string,
}

export interface IChannelIds {
    id: number
}