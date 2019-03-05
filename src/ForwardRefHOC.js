import React from 'react';

const withForwardedRef = Comp => {
    const handle = (props, ref) => <Comp {...props} forwardedRef={ref} />;

    const name = Comp.displayName || Comp.name;
    handle.displayName = `WithForwardedRef(${name})`;

    return React.forwardRef(handle);
};

export default withForwardedRef;