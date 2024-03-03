import {
  Button,
  Dropdown,
  DropdownButton,
  Nav,
  NavDropdown,
  Navbar,
} from "react-bootstrap";

const Footer = () => {
  return (
    <Navbar fixed="bottom">
      <Nav.Item>
        <Button>yrd</Button>
      </Nav.Item>
      <Nav.Item>
        <Nav variant="pills" activeKey="2" onSelect={(e) => console.log(e)}>
          <DropdownButton title={"test"} drop="up" >
           
             <Dropdown.Item eventKey="4.1">Action</Dropdown.Item>
              <Dropdown.Item eventKey="4.2">Another action</Dropdown.Item>
              <Dropdown.Item eventKey="4.3">
                Something else here
              </Dropdown.Item>
              <NavDropdown.Divider />
              <Dropdown.Item eventKey="4.4">Separated link</Dropdown.Item>
          </DropdownButton>
        </Nav>
      </Nav.Item>
    </Navbar>
  );
};

export default Footer;
