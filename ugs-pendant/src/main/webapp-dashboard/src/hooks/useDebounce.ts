import { useEffect, useState } from "react";

const useDebounce = <T>(value: T, delay?: number): T => {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    const timer = setTimeout(
      () => {
        console.log("Time out!"  + (!delay ? 500 : delay));
        setDebouncedValue(value);
      },
      !delay ? 500 : delay
    );

    return () => clearTimeout(timer);
  }, [value, delay]);

  return debouncedValue;
};

export default useDebounce;
