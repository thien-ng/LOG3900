export class Time {

    public static today(): string {
        const today = new Date();
        return this.formatTime(today.getDate()) + "/" +
            this.formatTime(today.getMonth() + 1) + "/" +
            this.formatTime(today.getFullYear()) + " " +
            this.now(today);
    }

    public static now(today: Date = new Date()): string {
        const hour: string = this.formatTime(today.getHours());
        const minute: string = this.formatTime(today.getMinutes());
        const second: string = this.formatTime(today.getSeconds());

        return hour + ":" + minute + ":" + second;
    }

    private static formatTime(time: number): string {
        return time > 9 ? time.toString() : `0${time}`;
    }
}
