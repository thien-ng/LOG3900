export interface IChannelMessage {
    channel_id: number,
    account_id: number,
    content:    string,
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