import { createCanvas } from "canvas";
import { categories } from "./categories";
import { ISuggestion } from "../../../interfaces/creator";

const format = require('string-format');
const axios = require('axios');

const URL = "https://quickdrawfiles.appspot.com/drawing/{0}?id={1}&key=AIzaSyC0U3yLy_m6u7aOMi9YJL2w1vWG4oI5mj0";
const strokeWidth = 4;

export class CreationAssist2 {

    public static async fetchSuggestion(): Promise<ISuggestion> {   
        
        const category = categories[this.generateRandom(345)];
        const idIndex = this.generateRandom(10);

        const url = format(URL, category, idIndex);
        const data = (await axios.get(url)).data

        const canvas = createCanvas(300, 300);
        const ctx = canvas.getContext("2d");

        ctx.lineJoin = ctx.lineCap = 'round';
        ctx.lineWidth = 4;
        ctx.strokeStyle = '#000000';
        ctx.beginPath();

        var {x, y} = this.transformPoint(data.drawing[0][0], data.drawing[0][1], data.drawing);
        ctx.moveTo(x, y);

        for(var i = 0; i < data.drawing.length; i++){
            var len = data.drawing[i][0].length;
            for(var j = 0; j < len; j++){
                
                const this_x = data.drawing[i][0][j];
                const this_y = data.drawing[i][1][j];
                const {x, y} = this.transformPoint(this_x, this_y, data.drawing);

                if(j === 0)
                    ctx.moveTo(x, y);
                if(j > 0)
                    ctx.lineTo(x, y);
            }
            ctx.stroke();
        }

        return {
            drawing: canvas.toDataURL("image/png").replace("image/png", "image/octet-stream"),
            object: category,
        }
    }

    private static transformPoint(x: number, y: number, drawings: number[][]) {
        const drawingProperties = this.getDrawingProperties(drawings);
        x = (x - drawingProperties.x) * 1 + strokeWidth;
        y = (y - drawingProperties.y) * 1 + strokeWidth;
        return {x: x, y: y};
    }

    private static getDrawingProperties(points: any){
        let x_range = {min: 99999, max: 0},
            y_range = {min: 99999, max: 0};
        for(let i = 0; i < points.length; i++){
            let stroke_len = points[i][0].length;
            for(let j = 0; j < stroke_len; j++){
            let this_x = points[i][0][j],
                this_y = points[i][1][j];
            if(this_x < x_range.min) x_range.min = this_x;
            else if(this_x > x_range.max) x_range.max = this_x;
            if(this_y < y_range.min) y_range.min = this_y;
            else if(this_y > y_range.max) y_range.max = this_y;
            }
        }
        return {
            x_range: x_range, 
            y_range: y_range, 
            width: x_range.max - x_range.min + (strokeWidth * 2), 
            height: y_range.max - y_range.min + (strokeWidth * 2),
            x: x_range.min,
            y: y_range.min
        };
    }

    private static generateRandom(max: number): number {
        return Math.floor((Math.random() * max));
    }

}