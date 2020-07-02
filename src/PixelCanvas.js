import React, { ReactNode, useState } from "react";
import { View, StyleSheet } from "react-native";

interface Props {
    pixelSize: number;
    cellClicked(
        startX: number,
        startY: number,
        width: number,
        height: number
    ): void;
}

export default (props: React.PropsWithChildren<Props>) => {
    const [width, setWidth] = useState(0);
    const [height, setHeight] = useState(0);
    const columns: number = width / props.pixelSize;
    const rows: number = Math.floor(height / props.pixelSize);

    let cells: Array<ReactNode> = [];
    let paths: Array<string> = [];

    const onTouch = (event, top, left) => {
        paths = [];
        props.cellClicked(left, top, props.pixelSize, props.pixelSize);
    };

    const onMove = (event, top, left, row) => {
        const xMovedOverBy: number = Math.floor(
            event.nativeEvent.locationX / props.pixelSize
        );
        const yMovedOverBy: number = Math.floor(
            event.nativeEvent.locationY / props.pixelSize
        );
        const path: string = `${xMovedOverBy},${yMovedOverBy}`;

        if (
            (xMovedOverBy !== 0 || yMovedOverBy !== 0) &&
            paths.indexOf(path) === -1 &&
            row + yMovedOverBy < rows
        ) {
            props.cellClicked(
                left + xMovedOverBy * props.pixelSize,
                top + yMovedOverBy * props.pixelSize,
                props.pixelSize,
                props.pixelSize
            );

            paths.push(path);

            console.log(`${xMovedOverBy}, ${yMovedOverBy}`);
        }
    };

    for (let row = 0; row < rows; row++) {
        for (let col = 0; col < columns; col++) {
            const top: number = row * props.pixelSize;
            const left: number = col * props.pixelSize;

            cells.push(
                <View
                    onStartShouldSetResponder={() => true}
                    onStartShouldSetResponderCapture={() => true}
                    onMoveShouldSetResponder={() => true}
                    onMoveShouldSetResponderCapture={() => true}
                    onResponderStart={(event) => onTouch(event, top, left)}
                    onResponderMove={(event) => onMove(event, top, left, row)}
                    key={`${row}-${col}`}
                    style={[
                        Style.pixel,
                        {
                            top: top,
                            left: left,
                            width: props.pixelSize,
                            height: props.pixelSize,
                        },
                    ]}
                />
            );
        }
    }

    return (
        <View
            onStartShouldSetResponder={() => true}
            style={Style.container}
            onLayout={(event) => {
                const { width, height } = event.nativeEvent.layout;

                setWidth(width);
                setHeight(height);
            }}
        >
            {props.children}
            {cells}
        </View>
    );
};

const Style = StyleSheet.create({
    container: {
        flex: 1,
    },
    pixel: {
        position: "absolute",
        borderStyle: "solid",
        borderWidth: 1,
        borderColor: "grey",
    },
});
