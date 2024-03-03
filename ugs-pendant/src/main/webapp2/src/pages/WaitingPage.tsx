import { useEffect } from "react";
import { Container, Spinner } from "react-bootstrap";
import { useAppSelector } from "../hooks/useAppSelector";
import { useDispatch } from "react-redux";
import { socketActions } from "../store/socketSlice";

const WaitingPage = () => {
  const dispatch = useDispatch();
  const { isConnected, isEstablishingConnection } = useAppSelector(
    (state) => state.socket
  );

  useEffect(() => {
    const timer = setTimeout(() => {
      dispatch(socketActions.connect());
    }, 4000);

    return () => clearTimeout(timer);
  }, [dispatch, isConnected, isEstablishingConnection]);

  return (
    <Container>
      Waiting for UGS to become active{" "}
      <Spinner animation="border" variant="primary" size="sm" />
    </Container>
  );
};

export default WaitingPage;
