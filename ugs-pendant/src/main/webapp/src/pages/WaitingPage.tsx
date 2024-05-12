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
    <Container style={{marginTop: "24px"}}>
      <h1>Establishing connection</h1>
      <p>Trying to establish connection to UGS <Spinner animation="border" variant="primary" size="sm" /></p>
    </Container>
  );
};

export default WaitingPage;
