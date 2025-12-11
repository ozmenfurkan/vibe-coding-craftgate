import { forwardRef, useEffect, useState } from 'react';
import Input, { InputProps } from '@/components/ui/Input';
import { formatExpireDate } from '@/lib/formatters';

interface ExpireDateInputProps extends Omit<InputProps, 'onChange'> {
  onChange: (value: string) => void;
}

/**
 * Expire Date Input Component with automatic formatting
 * Format: MM/YY
 */
const ExpireDateInput = forwardRef<HTMLInputElement, ExpireDateInputProps>(
  ({ value, onChange, ...props }, ref) => {
    const [displayValue, setDisplayValue] = useState('');

    useEffect(() => {
      if (typeof value === 'string') {
        setDisplayValue(formatExpireDate(value));
      }
    }, [value]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      const rawValue = e.target.value.replace(/\D/g, ''); // Sadece rakamlar
      onChange(rawValue);
    };

    return (
      <Input
        ref={ref}
        {...props}
        value={displayValue}
        onChange={handleChange}
        maxLength={5} // MM/YY
        placeholder="MM/YY"
        inputMode="numeric"
      />
    );
  }
);

ExpireDateInput.displayName = 'ExpireDateInput';

export default ExpireDateInput;

